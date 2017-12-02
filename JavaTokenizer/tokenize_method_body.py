import re

op_dict = {}
ignore_token_set = set()

# change for loop condition section into text and also modifying operators in initialization and incremental sections
def alter_for_loop(for_statement):
    for_fields = for_statement.groupdict()['condition'].split(';')
    for_fields[0] = for_fields[0].replace('=',' set to ')

    for op in op_dict:
        for_fields[1] = for_fields[2].replace(op,op_dict[op])
    for_fields[1] = ' until ' +for_fields[1]

    for op in op_dict:
        for_fields[2] = for_fields[2].replace(op,op_dict[op])

    return ' '.join(for_fields)

# split method body into lines and make replacements/omissions in each line
def split_statements(method_body):
    # print(op_dict)
    # print(ignore_token_set)
    #print('method_body:',method_body)
    method_body = re.sub(r'(for)(\()(?P<condition>([A-Za-z0-9,<>=\s_;.\(\)+\-\*\/]+))([\)])\s(\{)',alter_for_loop, method_body)
    for op in op_dict:
        method_body = method_body.replace(op,op_dict[op])
    for token in ignore_token_set:
        method_body = method_body.replace(token,' ')
    method_body = method_body.replace('_', ' ')
    return method_body