package parse;
import java.io.*;  
import java.util.*;  
  
public class Lexer {      
    char peek = ' ';     
    Hashtable<String, Word> words =   
            new Hashtable<String, Word>();  
    
    BufferedReader reader = null;   
 
    private Boolean isReaderEnd = false;   
      
    public Boolean getReaderState() {  
        return this.isReaderEnd;  
    }  

      
    private void reserve(Word w) {  
        words.put(w.lexme, w);  
    }  
      
  
    public Lexer(BufferedReader reader) {  
    	this.reader=reader;
     
        this.reserve(new Word("create",Tag.CREATE));
        this.reserve(new Word("drop",Tag.DROP));
        this.reserve(new Word("table",Tag.TABLE));
        this.reserve(new Word("index",Tag.INDEX));
        this.reserve(new Word("select",Tag.SELECT));
        this.reserve(new Word("insert", Tag.INSERT));  
        this.reserve(new Word("delete", Tag.DELETE));  
        this.reserve(new Word("quit", Tag.QUIT));  
        this.reserve(new Word("execfile", Tag.EXECFILE));  
        this.reserve(new Word("show",Tag.SHOW));
        this.reserve(new Word("from",Tag.FROM));
        this.reserve(new Word("into",Tag.INTO));
        this.reserve(new Word("where",Tag.WHERE));
        this.reserve(new Word("on",Tag.ON));
        this.reserve(new Word("and",Tag.AND));
        this.reserve(new Word("or",Tag.OR));
        this.reserve(new Word("unique",Tag.UNIQUE));
        this.reserve(new Word("primary",Tag.PRIMARY));
        this.reserve(new Word("key",Tag.KEY));
        this.reserve(new Word("values",Tag.VALUES));
        this.reserve(new Word("order",Tag.ORDER));
        this.reserve(new Word("by",Tag.BY));
        this.reserve(new Word("asc",Tag.ASC));
        this.reserve(new Word("desc",Tag.DESC));
        this.reserve(new Word("join",Tag.JOIN));
        this.reserve(new Word("int", Tag.TYPE));  
        this.reserve(new Word("float", Tag.TYPE)); 
        this.reserve(new Word("char", Tag.TYPE)); 
    }  
      
    private void readch() throws IOException  {   
		peek = (char)reader.read();
        if((int)peek == 0xffff){  
	         this.isReaderEnd = true;  
	     }  
    } 

      
    private Boolean readch(char ch) throws IOException {  
        readch();  
        if (this.peek != ch) {  
            return false;  
        }  
          
        this.peek = ' ';  
        return true;  
    }  
    
    public Token scan() throws IOException {  
     
        for( ; ; readch() ) {  
            if(peek == ' ' || peek == '\t'||peek=='\r')  
                continue;  
            else if (peek == '\n')   ;
            
            else  
                break;  
        }  
          
  
        switch (peek) {  
        case '=' :  
            if (readch('=')) {  
                return Comparison.eq;   
            }  
            else {  
                return Comparison.eq;  
            }  
        case '>' :  
            if (readch('=')) {   
                return Comparison.ge;  
            }  
            else {  
                return Comparison.gt;  
            }  
        case '<' :  
            if (readch('=')) {   
                return Comparison.le;  
            }  
            else if(this.peek=='>'){
            	this.peek = ' ';  
                 return Comparison.ne;  
            }
            else {  
                return Comparison.lt;  
            }  
        case '!' :  
            if (readch('=')) {    
                return Comparison.ne;  
            }  
            else {  
                return new Token('!');  
            }     
        }  
        
        
        if(Character.isDigit(peek)) {  
            double value = 0;  Num n;
            do {  
            	
                value = 10 * value + Character.digit(peek, 10);  
                readch();  
                
            } while (Character.isDigit(peek));  
            if(peek=='.'){
            	readch();int i=1;
            	do {  
                	
                    value = value + Character.digit(peek, 10)*Math.pow(0.1, i++);  
                    readch();  
                    
                } while (Character.isDigit(peek)); 
            	n = new Num((float)value); 
            }               
            else{
            	n = new Num((int)value);  
            }
             
            //table.put(n, "Num");  
            return n;  
        }  
  
        if(peek=='\''){
        	StringBuffer sb = new StringBuffer();
        	sb.append("");
      
        	readch(); 
        	
        	while (peek!='\''&&peek!=';'){
        		 sb.append(peek);  
                 readch(); 
        	}
        	
   
        	Token w;
        	if(peek==';') {
        		 w  = new Token(peek);  
        	}
        	else
        	 	 w = new Word(sb.toString(), Tag.STR);  
        	readch();       
            return w; 
        }
  
        if(Character.isLetter(peek)) {  
        	
            StringBuffer sb = new StringBuffer();  
              
            do {  
                sb.append(peek);  
                readch();  
            } while (Character.isLetterOrDigit(peek)||peek=='_'||peek=='.'||peek=='&');  
              
            String s = sb.toString();  
            Word w = (Word)words.get(s);  
            
            if(w != null) {  
                return w; 
            }  
              
            w = new Word(s, Tag.ID);  
            
            return w;  
        }  
          
        Token tok  = new Token(peek);  
        peek = ' ';  
          
        return tok;  
    }  
}  