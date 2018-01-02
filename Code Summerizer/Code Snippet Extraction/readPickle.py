import pickle
import codecs

itemlist = []

with open ("Output Folder/code_comments.pkl", 'rb') as fp:
	itemlist = pickle.load(fp)

commentsList = itemlist[0]
codeList = itemlist[1]

for i in range(0, len(commentsList)):
	print("Comment:")
	print(commentsList[i])
	print("Code:")
	print(codeList[i])
	print("---------------------")