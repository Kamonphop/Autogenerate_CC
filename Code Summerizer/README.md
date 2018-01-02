Read Me

Extracting Code and comments from OOS Projects 

1. Download an OSS project from Github and save it in **Code Snippet Extraction/Data Folder**
2. Run **trainingDataWord2Vec.py** to extract the comments and code
	```python2 trainingDataWord2Vec.py "Data Folder/"```
	[requires Python 2 for this code to run]
3. This outputs a pickle file - **Output Folder/code_comments.pkl**
	[To read the pickle file python readPickle.py]

To covert code to more English like sentences 

1. Go into the **JavaTokenizer/**
2. Run 
	```python source_to_text.py```
		
To Train Glove

1. Go into **Glove/**
2. Run 
	```python getGloveInput.py```
   This outputs a file - **Output/GloveInput**, which will be used to train glove
3. Run the Stanford tokenizer on GloveInput file by running
	```java -cp stanford-postagger.jar edu.stanford.nlp.process.PTBTokenizer Output/GloveInput > Output/vocab.txt```
4. To train the glove use Autogenerate_CC/Glove/GloVe-1.2/demo.sh as reference
	I trained the glove model using the following, (modify based on you need)
	```CORPUS=Output/GloveInput
	VOCAB_FILE=Output/vocab.txt
	COOCCURRENCE_FILE=Output/cooccurrence.bin
	COOCCURRENCE_SHUF_FILE=Output/cooccurrence.shuf.bin
	BUILDDIR=Autogenerate_CC/Glove/GloVe-1.2/build
	SAVE_FILE=Output/vectors
	VERBOSE=2
	MEMORY=4.0
	VOCAB_MIN_COUNT=5
	VECTOR_SIZE=50
	MAX_ITER=15
	WINDOW_SIZE=15
	BINARY=2
	NUM_THREADS=8
	X_MAX=10
	$BUILDDIR/vocab_count -min-count $VOCAB_MIN_COUNT -verbose $VERBOSE < $CORPUS > $VOCAB_FILE
	$BUILDDIR/cooccur -memory $MEMORY -vocab-file $VOCAB_FILE -verbose $VERBOSE -window-size $WINDOW_SIZE < $CORPUS > $COOCCURRENCE_FILE
	$BUILDDIR/shuffle -memory $MEMORY -verbose $VERBOSE < $COOCCURRENCE_FILE > $COOCCURRENCE_SHUF_FILE
	$BUILDDIR/glove -save-file $SAVE_FILE -threads $NUM_THREADS -input-file $COOCCURRENCE_SHUF_FILE -x-max $X_MAX -iter $MAX_ITER -vector-size $VECTOR_SIZE -binary $BINARY -vocab-file $VOCAB_FILE -verbose $VERBOSE```

Follow https://github.com/llSourcell/How_to_make_a_text_summarizer for the rest.

1. Use Theano Backend
2. Python 2 prefered (requires small changes to be done to be run in python 3)
