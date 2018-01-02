# Autogenerate_CC


Extracting Code and comments from OOS Projects 

1. Download an OSS project from Github and save it in 
		Code Snippet Extraction/Data Folder
2. Run trainingDataWord2Vec.py to extract the comments and code
		python2 trainingDataWord2Vec.py "Data Folder/"
		[requires Python 2 for this code to run]
3. This outputs a pickle file "code_comments.pkl" in the output folder
		[To readthe pickle file python readPickle.py]

To covert code to more English like sentences 

1. Go into the JavaTokenizer/ folder
2. Run 
		python source_to_text.py

To Train Glove
1. Go into Glove Folder 
2. Run 
		python getGloveInput.py
3. This outputs a file GloveInput, which will be used to train glove
4. Run the stanford tokenizer on GloveInput file by running
		java -cp stanford-postagger.jar edu.stanford.nlp.process.PTBTokenizer Output/GloveInput > Output/vocab.txt
5. 