package parse;

public class Word extends Token {  
    public String lexme = "";  
      
    public Word (String s, int t) {  
        super(t);  
        this.lexme = s;  
    }  
      
    public String toString() {  
        return this.lexme;  
    }  
}  