import re
import tokenize_method_body as tmb
# from tokenize_method_body import op_dict, ignore_token_set
import pickle

#--- remove abbreviation and unnecessary symbols and replace them with meaningful text ---#
def expand_params(func_params):
    func_params = replace_abbr(func_params).replace(',',' and ')
    for token in tmb.ignore_token_set:
        func_params = func_params.replace(token,'')
    return ' takes '+func_params

#---method to replace abbreviations from built dictionary---#
def replace_abbr(abbr_str):
    for abbr in abbr_dict:
        # if ' '+abbr+' ' in abbr_str:
        #     print('>>>>>>>>>>>>>>>>>>'+abbr_str)
        abbr_str = abbr_str.replace(' '+abbr+' ',' '+abbr_dict[abbr]+' ')
        # if abbr=='ctx':
        #    print('>>>>>>>>>>>>>',abbr_dict[abbr])

    return abbr_str

#---method that removes angular brackets---#
def remove_cascade(angular_str):
    return angular_str.groupdict()['type_name']+' of type '

#---clean header to remove unnecessary symbols like angular brackets---#
def clean_header(header_dict):
    # uses global argument abbr_dict
    header_grps = header_dict.groupdict()
    header_grps['type'] = re.sub(r'(?P<type_name>[A-Za-z_0-9]+)(?P<cascade>[<]+)', remove_cascade,header_grps['type'])
    header_grps['type'] = header_grps['type'].replace('>',' ').replace('_',' ')
    return header_grps['name']+ ' returns '+replace_abbr(header_grps['type'])

#---parse method declaration part and remove access specifiers and break name of method---#
def parse_method_decl(func_decl):

    #regex for matching general method header
    header_expr=r'(?P<acc>(public|private|protected)?)(\s)(?P<opt1>(static|final)?)(\s)(?P<opt2>(static |final )?)' \
                r'(?P<type>[A-Za-z<>]+)(\s)(?P<name>[A-Za-z0-9_]+)'

    func_decl = re.sub(header_expr,clean_header,func_decl)
    for token in tmb.ignore_token_set:
        func_decl = func_decl.replace(token,'')
    #print('func_decl:',func_decl)
    return func_decl

#---parse function header and split it into: declaratiom=n, arguments and body---#
def parse_input(input_method):
    method_expr = r'(?P<func_decl>[a-zA-Z_\s<>]*)\((?P<func_params>[a-zA-Z0-9\s,<>_]*)\)\s*{(?P<func_body>.*)\s*}'
    matches = re.search(method_expr,input_method)
    if matches == None:
        return None
    sections = matches.groupdict()
    sections['func_decl'] = parse_method_decl(sections['func_decl'])
    sections['func_params'] = expand_params(sections['func_params'])
    sections['func_body'] = tmb.split_statements(sections['func_body'])
    return sections

#---add underscores for snake casing the words---#
def replace_underscore(x):
    #replacing camel casing with snake casing for readability
    return '_'+x.groupdict()['camel']

#---change camel casing to snake casing---#
def camel_to_snake(input_string):
    #substituting camel casing with snake casing
    return re.sub(r'(?P<camel>[A-Z][a-z]+)', replace_underscore , input_string)

# def read_input(input_file_path):
#     with open(input_file_path,'r') as input_file:
#         for method in input_file:
#             method_snake = camel_to_snake(method)
#             print('method:',method_snake)
#             func_sections = parse_input(method_snake)
#             func_translation = func_sections['func_decl']+' takes '+func_sections['func_params']+' and runs as follows: '


#---main method: reads pickle file, removes newline chars and write to new pickle file---#
def read_pickle_input(input_file_path,output_file_path):
    with open(input_file_path, 'rb') as fp:
        itemlist = pickle.load(fp)
    formattedCodeList=[]
    code_list = itemlist[1]
    comments_list = itemlist[0]
    for i in range(len(code_list)):
        code = code_list[i]
        # print(code)
        # print('Code--------------------------------------')
        # input()
        method=''
        for line in code.split('\n'):
            method+=chr(12)+re.sub(r'\s*//.*','',line)

        # print(method)
        # print('Cleaned-----------------------------------')
        method_snake = camel_to_snake(method)
        func_sections = parse_input(method_snake)
        if func_sections==None:
            formattedCodeList.append(' ')
            continue
        summary = func_sections['func_decl']+func_sections['func_params']+' and runs as follows '+func_sections['func_body']
        summary = re.sub(r'\s+',' ',summary).lower()
        #summary = summary.replace('_',' ').replace(chr(12),'\n')
        summary = summary.replace('_', ' ')
        # print(summary)
        # print('Summary-----------------------------------')
        #input()

        summary = summary.replace(chr(12), '\n')
        # print(summary)
        # print('Summary:2---------------------------------')
        # input()
        formattedCodeList.append(summary)


    mytup = (comments_list,formattedCodeList,None)
    pickle.dump( mytup, open( output_file_path, "wb" ))
    # with open(output_file_path, 'wb') as f:
    #     pickle.dump(formattedCodeList, f)
    #     pickle.dump(comments_list, f)
    # f.close()

#---build dictionary of abbreviations and their expansions---#
def build_abbr_dict(abbr_file_path):
    abbr_dict= dict()
    with open(abbr_file_path,'r') as abbr_file:
        for line in abbr_file:
            abbr,expansion = line.strip().split(':=')
            abbr_dict[abbr] = expansion
    return abbr_dict

#---build a dictionary with key as operator and value as the text replacing the operator---#
def build_op_dict(op_dict_file):
    op_dict={}
    operators = open(op_dict_file, 'r')
    for op in operators:
        op_fields= op.strip().split(':')
        op_dict[op_fields[0]] = " ".join(op_fields[1:])
    return op_dict

#-- build a set of keywods to be ignored---#
def read_keyword_ops(keyword_set_file):
    ignore_token_set =set()
    keywords = open(keyword_set_file,'r')
    for keyword in keywords:
        ignore_token_set.add(keyword.strip())
    return ignore_token_set


#--build dictionary for replacements and omissions; call the main method--#
tmb.ignore_token_set |= read_keyword_ops('ignore_token_list.txt')
tmb.op_dict.update(build_op_dict('operator_list.txt'))
abbr_dict = build_abbr_dict('abbreviations.txt')
#read_input('coherence_code.txt')
read_pickle_input("../Code Snippet Extraction/Output Folder/code_comments.pkl","formatted_code_comments.pkl")