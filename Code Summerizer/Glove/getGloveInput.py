import pickle
import codecs

itemlist = []
f = codecs.open("Output/GloveInput", "w", "utf-8")
with open ("../JavaTokenizer/formatted_code_comments.pkl", 'rb') as fp:
	itemlist = pickle.load(fp)
commentsList = itemlist[0]
codeList = itemlist[1]
for i in range(0, len(commentsList)):
	print("Comment:")
	print(commentsList[i])
	f.write(commentsList[i])
	print("Code:")
	print(codeList[i])
	f.write(codeList[i])
	print("---------------------")