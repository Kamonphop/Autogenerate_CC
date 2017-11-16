import sys
import os
import re
import Queue
import json
import cPickle as pickle
import codecs

commentsList = []
codeList = []

def main(walkDir):
	getJavaFiles(walkDir)
	getFileInformation(javaFiles)

def getJavaFiles(workingdir):
	for root, subdirs, files in os.walk(workingdir):
		for subdir in subdirs:
		    getJavaFiles(subdir)
		for filename in files:
			if(filename.endswith('.java') and re.search("target", root) == None):
				file_path = os.path.join(root, filename)
				javaFiles.append(file_path)

def getFileInformation(fileList):
	for file in fileList:
		commentCodeMap = getCommentCodeMap(file)

def getCommentCodeMap(file):
	try:
		f = codecs.open(file, "r", "utf-8")

		# first while is for the entire file (handles multiple class in the same file)
		lines = f.readlines()
		lineNo = 0
		
		while(lineNo < len(lines)):
			line = lines[lineNo].strip()	

			# finding the classes
			# if the word class is not in a comment
			if(re.search("class ", line) and re.search("[/*]",line) == None):
				br_q = Queue.Queue()

				#checks for start of class {
				if(re.search("{", line)):
					br_q.put("{")
					lineNo+=1
				elif (re.search("{", lines[lineNo+1])):
					lineNo += 1
					line = lines[lineNo].strip()
					br_q.put("{")
					lineNo+=1
				else:
					# print("Couldnt identify the class")
					break

				while(not br_q.empty() and lineNo < len(lines)):
					code = ""
					comment = ""
					line = lines[lineNo].strip()

					# finds comment
					if(line.startswith("/*")):
						comment = ""
						while(not line.endswith("*/")):
							comment+=line+"\n"
							lineNo+=1
							line = lines[lineNo].strip()
						comment+=line+"\n"

						#line after the comment
						lineNo += 1
						line = lines[lineNo].strip()
						if(not line.endswith(";")):
							if(re.search("{", line)):
								br_q.put("{")
								code += line+"\n"
								if(re.search("}", line)):
									br_q.get()
								lineNo +=1
								line = lines[lineNo].strip()
							elif( re.search("{", lines[lineNo+1])):
								code += line+"\n"
								lineNo +=1
								line = lines[lineNo].strip()
								br_q.put("{")
								code+=line
								if(re.search("}", line)):
									br_q.get()
								lineNo+=1
								line = lines[lineNo].strip()
							while(br_q.qsize() >1):
							 	# start of a method
								if(re.search("{", line)):
									br_q.put("{")
								if(re.search("}", line)):
									br_q.get()
								code += line+"\n"
								lineNo += 1
								line = lines[lineNo].strip()
							if(comment!="" ):
								commentsList.append(comment)
								codeList.append(code)
								# print("------------------------")
								# print("Comment:")
								# print(comment)
								# print("Code:")
								# print(code)
								# print("------------------------")

					# encounters a block of statments
					if(re.search("{", line)):
						br_q.put("{")
					if(re.search("}", line)):
						br_q.get()
					lineNo += 1
			lineNo += 1
		f.close()
	except:
		#print("Skiped "+ file)
		pass

			
if __name__ == '__main__':
	javaFiles = []
	main(sys.argv[1])
	print("Total code snippets found " +str(len(commentsList)))

	# write pickle file
	mytup = (commentsList,codeList,None)
	pickle.dump( mytup, open( "code_comments.pkl", "wb" ))
	
	#read pickle file
	# with open ("code_comments.pkl", 'rb') as fp:
	# 	itemlist = pickle.load(fp)
	# commentsList = itemlist[0]
	# codeList = itemlist[1]
