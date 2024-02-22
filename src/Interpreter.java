import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Vector;

import com.yjb.core.catmgr.*;
import com.yjb.core.recmgr.*;
import com.yjb.core.parse.*;

public class Interpreter {
   private static Token thetoken;
   private static boolean isSynCorrect=true;
   private static boolean isSemaCorrect=true;
   private static String synErrMsg;
   private static String semaErrMsg;
   
   public static void main(String []args){		   
	   System.out.println("Welcome to MiniSql.Please enter the command");
	   
	   try{
		    API.Initialize();
		    BufferedReader reader=new BufferedReader(new InputStreamReader(System.in));
		   Parsing(reader);    
        }
       catch(Exception e){
    	   System.out.println("Interpreter error:"+e.getMessage());
    	   e.printStackTrace();
       }
   }

   public static void Parsing(BufferedReader reader)throws IOException {
	   Lexer lexer = new Lexer(reader);  
   		while(lexer.getReaderState() == false){ 
   			
   			if(!isSynCorrect){   
   				if(thetoken.toString().equals(";")){
   					System.out.println(synErrMsg);
   					isSemaCorrect=true;
   					isSynCorrect=true;
   					continue;
   				}
   			}
   			thetoken=lexer.scan();	   
   			 if(thetoken.tag==Tag.EXECFILE){
   				thetoken=lexer.scan();	
			    
   			    File file=new File(thetoken.toString()+".txt");
   			    thetoken=lexer.scan();
			     if(thetoken.toString().equals(";")){
			    	
			    	if(file.exists()){
			    		 BufferedReader reader2=new BufferedReader(new FileReader(file));
			    		 Parsing(reader2);
			    		 isSynCorrect=true;
			    		 continue;
			    	 }
			    	else{
			    		 synErrMsg="The file "+file.getName()+" doesn't exist";
			    		 isSynCorrect=false;
			    		 continue;
			    	 }
					 
				 }
				 else{
					 if(isSynCorrect)  synErrMsg="Synthetic error near: "+thetoken.toString();isSynCorrect=false;
					 continue;
				 }		   
			 }				   
			 else if(thetoken.tag==Tag.QUIT){
				 thetoken=lexer.scan();
				 if(thetoken.toString().equals(";")){
					 System.out.println("Quit the MiniSql. See you next time!");
					    API.close();
					    
					    reader.close();
					 System.exit(0);						 
				 }
				 else{
					 if(isSynCorrect)  synErrMsg="Synthetic error near: "+thetoken.toString();isSynCorrect=false;
					 continue;
				 }
				 
			 }
			 else if(thetoken.tag==Tag.CREATE){
				 thetoken=lexer.scan();
			
				 if(thetoken.tag==Tag.TABLE){
					thetoken=lexer.scan();
					if(thetoken.tag==Tag.ID){													 
						    String tmpTableName=thetoken.toString();
							Vector<Attribute>tmpAttributes=new Vector<Attribute>();
							String tmpPrimaryKey=null;
							 if(CatalogManager.isTableExist(tmpTableName)){
								 semaErrMsg="The table "+tmpTableName+" already exists";
								 isSemaCorrect=false;	 
							 }
						 thetoken=lexer.scan();
						 if(thetoken.toString().equals("(")){
							 thetoken=lexer.scan();			
							 while(!thetoken.toString().equals(")")&&!thetoken.toString().equals(";")){
								 if(thetoken.tag==Tag.ID){ 
									 String tmpAttriName=thetoken.toString();
									 String tmpType;
									 int tmpLength;
									 boolean tmpIsU=false;
									 if(CatalogManager.isAttributeExist(tmpAttributes, tmpAttriName)){
										 semaErrMsg="Duplicated attribute names "+tmpAttriName;
										 isSemaCorrect=false;
									 }
									 thetoken=lexer.scan();
									 if(thetoken.tag==Tag.TYPE){
										 tmpType=thetoken.toString();
										 if(tmpType.equals("char")){
											 thetoken=lexer.scan();
											 if(thetoken.toString().equals("(")){
												 thetoken=lexer.scan();
												  if(thetoken.tag==Tag.INTNUM){
													  tmpLength = Integer.parseInt(thetoken.toString());
													  if(tmpLength<1||tmpLength>255){														  
															semaErrMsg="The length of char should be 1<=n<=255";
															isSemaCorrect=false;
													  }	
													  thetoken=lexer.scan();
													  if(thetoken.toString().equals(")"));
													  else{
														 if(isSynCorrect)  synErrMsg="Synthetic error near: "+thetoken.toString();isSynCorrect=false;
														 break;
													  }
												  }	
												  else{
													 if(isSynCorrect)  synErrMsg="Synthetic error near: "+thetoken.toString();isSynCorrect=false;
													 break;
												  }
											 }									
											 else{
												 if(isSynCorrect)  synErrMsg="Synthetic error near: "+thetoken.toString();isSynCorrect=false;
												 break;
											 }
											 
										 }	
										 else{
											 tmpLength=4;
											 
										 }
										 thetoken=lexer.scan();
										 if(thetoken.tag==Tag.UNIQUE){
											 tmpIsU=true;
											 thetoken=lexer.scan();										 
										 }
										 else ;
										 if(thetoken.toString().equals(",")){											 
											 tmpAttributes.addElement(new Attribute(tmpAttriName,tmpType,tmpLength,tmpIsU));
										 }
										 else if(thetoken.toString().equals(")")){
											 tmpAttributes.addElement(new Attribute(tmpAttriName,tmpType,tmpLength,tmpIsU));
											 break;
										 }
										 else{
											 if(isSynCorrect)  synErrMsg="Synthetic error near: "+thetoken.toString();isSynCorrect=false;
											 break;
										 }
									 }
									 else{	 
										 if(isSynCorrect)  synErrMsg="Synthetic error near: "+thetoken.toString();isSynCorrect=false;
										 break;
									 }
								 }
								 
								 else if(thetoken.tag==Tag.PRIMARY){
									 thetoken=lexer.scan();
									 if(thetoken.tag==Tag.KEY){
										 thetoken=lexer.scan();
										 if(thetoken.toString().equals("(")){
											 thetoken=lexer.scan();
											  if(thetoken.tag==Tag.ID){
												  tmpPrimaryKey=thetoken.toString();
												  thetoken=lexer.scan();
												  if(thetoken.toString().equals(")")){
													if(!CatalogManager.isAttributeExist(tmpAttributes, tmpPrimaryKey)){
														semaErrMsg="The attribute "+tmpPrimaryKey+" doesn't exist";isSemaCorrect=false;
													}
												  }
												  else{
													
													 if(isSynCorrect)  synErrMsg="Synthetic error near: "+thetoken.toString();isSynCorrect=false;
													 break;
												  }
											  }	
											  else{
												 if(isSynCorrect)  synErrMsg="Synthetic error near: "+thetoken.toString();isSynCorrect=false;
												 break;
											  }
										 }									
										 else{
											 if(isSynCorrect)  synErrMsg="Synthetic error near: "+thetoken.toString();isSynCorrect=false;
											 break;
										 }
									 }
									 else{
										
										 if(isSynCorrect)  synErrMsg="Synthetic error near: "+thetoken.toString();isSynCorrect=false;
										 break;
									 }
								 }
								 
								 else{
									 if(isSynCorrect)  synErrMsg="Synthetic error near: "+thetoken.toString();isSynCorrect=false;
									 break;
								 }
								 thetoken=lexer.scan();
							 }
							 thetoken=lexer.scan();
							
							 if(isSynCorrect&&thetoken.toString().equals(";")){
								 
								 if(tmpPrimaryKey==null){
									 synErrMsg="Synthetic error: no primary key defined";isSynCorrect=false;
									 continue;
								 }
								 if(isSemaCorrect){
									 if(API.createTable(tmpTableName,new Table(tmpTableName,tmpAttributes,tmpPrimaryKey)))
									 System.out.println("create table "+tmpTableName+" succeeded");
									 else
										 System.out.println("Error: create table failed");
									
								 }
								 else{
									 System.out.print(semaErrMsg);
									 System.out.println(", create table "+tmpTableName+" failed");
									 isSemaCorrect=true;
								 }
								 continue;
							 }
							 else{
								 
								 if(isSynCorrect)  synErrMsg="Synthetic error near: "+thetoken.toString();isSynCorrect=false;
								 continue;
							 }
						 }						
					}
					 else{
						 if(isSynCorrect)  synErrMsg="Synthetic error near: "+thetoken.toString();isSynCorrect=false;
						 continue;
					 }					 						 
				 }
				
				 else if(thetoken.tag==Tag.INDEX){		
					 String tmpIndexName,tmpTableName,tmpAttriName;
					 thetoken=lexer.scan();
					 if(thetoken.tag==Tag.ID){
						 tmpIndexName=thetoken.toString();
						 if(CatalogManager.isIndexExist(tmpIndexName)){
							 semaErrMsg="The index "+tmpIndexName+" already exist";
							 isSemaCorrect=false;
						 }
						 thetoken=lexer.scan();
						 if(thetoken.tag==Tag.ON){
							 thetoken=lexer.scan();
							 if(thetoken.tag==Tag.ID){
								 tmpTableName=thetoken.toString();
								 if(!CatalogManager.isTableExist(tmpTableName)){
									 semaErrMsg="The table "+tmpTableName+" doesn't exist";
									 isSemaCorrect=false;
								 }
								 thetoken=lexer.scan();
								 if(thetoken.toString().equals("(")){
									 thetoken=lexer.scan();
									 if(thetoken.tag==Tag.ID){
										 tmpAttriName=thetoken.toString();
										 if(isSemaCorrect&&!CatalogManager.isAttributeExist(tmpTableName, tmpAttriName)){
											 semaErrMsg="The attribute "+tmpAttriName+" doesn't exist on "+tmpTableName;
											 isSemaCorrect=false;
										 }
										 else if(isSemaCorrect&&!CatalogManager.inUniqueKey(tmpTableName, tmpAttriName)){
											 semaErrMsg="The attribute "+tmpAttriName+" on "+tmpTableName+" is not unique";
											 isSemaCorrect=false;
										 }
										 else if(isSemaCorrect&&CatalogManager.isIndexKey(tmpTableName, tmpAttriName)){
											 semaErrMsg="The attribute "+tmpAttriName+" on "+tmpTableName+" is already an index";
											 isSemaCorrect=false;
										 }
										 thetoken=lexer.scan();
										 if(thetoken.toString().equals(")")&&lexer.scan().toString().equals(";")){
											 
											 if(isSemaCorrect){
												 if(API.createIndex(new Index(tmpIndexName,tmpTableName,tmpAttriName)))
												  System.out.println("create index "+tmpIndexName+" on "+tmpTableName+" ("+tmpAttriName+") succeeded.");
												 else
													 System.out.println("Error:create index failed");
											 }
											 else{
												 System.out.print(semaErrMsg);
												 System.out.println(", create index failed");
												 isSemaCorrect=true;
											 }
										 }	
										 else{
											 if(isSynCorrect)  synErrMsg="Synthetic error near: "+thetoken.toString();isSynCorrect=false;
											 continue;
										 }
									 }
									 else{
										 if(isSynCorrect)  synErrMsg="Synthetic error near: "+thetoken.toString();isSynCorrect=false;
										 continue;
									 }
																 
								 }
								 else{
									 if(isSynCorrect)  synErrMsg="Synthetic error near: "+thetoken.toString();isSynCorrect=false;
									 continue;
								 }
							 }
							 else{
								 if(isSynCorrect)  synErrMsg="Synthetic error near: "+thetoken.toString();isSynCorrect=false;
								 continue;
							 }
						 }
						 else{
							 if(isSynCorrect)  synErrMsg="Synthetic error near: "+thetoken.toString();isSynCorrect=false;
							 continue;
						 }
					 }
					 else{
						 if(isSynCorrect)  synErrMsg="Synthetic error near: "+thetoken.toString();isSynCorrect=false;
						 continue;
					 }	
				 }
				 else{
					 if(isSynCorrect)  synErrMsg="Synthetic error near: "+thetoken.toString();isSynCorrect=false;
					 continue;
				 }
			 }
			 else if(thetoken.tag==Tag.DROP){
				 thetoken=lexer.scan();
				
				 if(thetoken.tag==Tag.TABLE){
					 String tmpTableName;
					 thetoken=lexer.scan();
					 if(thetoken.tag==Tag.ID){
						 tmpTableName=thetoken.toString();
						 if(!CatalogManager.isTableExist(tmpTableName)){
							 semaErrMsg="The table "+tmpTableName+" doesn't exist, ";
							 isSemaCorrect=false;
						 }
						 thetoken=lexer.scan();
						 if(thetoken.toString().equals(";")){
							
							 if(isSemaCorrect){
								 if(API.dropTable(tmpTableName));
								 System.out.println("drop table "+tmpTableName+" succeeded");
							 }
							 else{
								 System.out.print(semaErrMsg);
								 System.out.println("drop table "+tmpTableName+" failed");
								 isSemaCorrect=true;
							 }
							 continue;
						 }
						 else{
							 if(isSynCorrect)  synErrMsg="Synthetic error near: "+thetoken.toString();isSynCorrect=false;
							 continue;
						 }
							 
					 }
					 else{
						 if(isSynCorrect)  synErrMsg="Synthetic error near: "+thetoken.toString();isSynCorrect=false;
						 continue;
					 }	 
				 }
				 else if(thetoken.tag==Tag.INDEX){
					 thetoken=lexer.scan();
					 if(thetoken.tag==Tag.ID){
						 String tmpIndexName=thetoken.toString();
						 if(!CatalogManager.isIndexExist(tmpIndexName)){
							 semaErrMsg="The index "+tmpIndexName+" doesn't exist, ";
							 isSemaCorrect=false;
						 }
						 if(tmpIndexName.endsWith("_prikey")){
							 semaErrMsg="The index "+tmpIndexName+" is a primary key, ";
							 isSemaCorrect=false;
						 }
						 thetoken=lexer.scan();
						 if(thetoken.toString().equals(";")){
							
							 if(isSemaCorrect){
								 if(API.dropIndex(tmpIndexName))
								 System.out.println("drop index "+tmpIndexName+" succeeded.");
							 }
							 else{
								 System.out.print(semaErrMsg);
								 System.out.println("drop index "+tmpIndexName+" failed");
								 isSemaCorrect=true;
							 }
							 continue;
						 }
						 else{
							 if(isSynCorrect)  synErrMsg="Synthetic error near: "+thetoken.toString();isSynCorrect=false;
							 continue;
						 }								 
					 }
					 else{
						 if(isSynCorrect)  synErrMsg="Synthetic error near: "+thetoken.toString();isSynCorrect=false;
						 continue;
					 }	 
				 }
				 else{
					 if(isSynCorrect)  synErrMsg="Synthetic error near: "+thetoken.toString();isSynCorrect=false;
					 continue;
				 }
			 }
			 else if(thetoken.tag==Tag.INSERT){
				 thetoken=lexer.scan();
				 if(thetoken.tag==Tag.INTO){
					 thetoken=lexer.scan();
					 if(thetoken.tag==Tag.ID){
						 String tmpTableName=thetoken.toString();
						 Vector<String>units=new Vector<String>();
						 if(!CatalogManager.isTableExist(tmpTableName)){
							 semaErrMsg="The table "+tmpTableName+" doesn't exist";
							 isSemaCorrect=false;
						 }
				
						 thetoken=lexer.scan();
						 if(thetoken.tag==Tag.VALUES){
							 thetoken=lexer.scan();
							 if(thetoken.toString().equals("(")){
								 thetoken=lexer.scan();
								 String tmpValue ;
								 int i=0;
								 while(!thetoken.toString().equals(")")){	
									
									 if(isSemaCorrect&&i>=CatalogManager.getTableAttriNum(tmpTableName)){
										 isSemaCorrect=false;
										 semaErrMsg="The number of values is larger than that of attributes";
									 }
									 else if(isSemaCorrect){
										 
										 tmpValue=thetoken.toString();
										 int tmpLength=CatalogManager.getLength(tmpTableName, i);
										 String tmpType=CatalogManager.getType(tmpTableName, i);
										 String tmpAttriName=CatalogManager.getAttriName(tmpTableName, i);
						
										 if(CatalogManager.inUniqueKey(tmpTableName, tmpAttriName)){
											 ConditionNode tmpCondition=new ConditionNode(tmpAttriName,"=",thetoken.toString());
											
											 if(isSemaCorrect&&API.selectTuples(tmpTableName,null,tmpCondition).size()!=0){
												 isSemaCorrect=false;
												 semaErrMsg="The value "+thetoken.toString()+" already exists in the unique attrubute "+tmpAttriName;
											 }
										 }
										 if(thetoken.tag==Tag.STR){
											 
											 
											 if(!tmpType.equals("char")
													 ||tmpLength<tmpValue.getBytes().length){
												 isSemaCorrect=false;
												 semaErrMsg="The type of value +"+tmpValue+" should be "+tmpType+"("+tmpLength+"), not char("+tmpValue.getBytes().length+")";
											 }
											 i++;
											 units.add(tmpValue); 
										 }
										 else if(thetoken.tag==Tag.INTNUM){
									
											 if(!tmpType.toString().equals("int")
													 &&!tmpType.equals("float")){
												isSemaCorrect=false;
												semaErrMsg="The type of value +"+tmpValue+" should be "+tmpType+"("+tmpLength+"), not be int";
											 }
											 i++;
											 units.add(tmpValue); 	 
										 }
										 else if(thetoken.tag==Tag.FLOATNUM){
									
											 if(!CatalogManager.getType(tmpTableName, i++).equals("float")){
												isSemaCorrect=false;
												semaErrMsg="The type of value +"+tmpValue+" should be "+tmpType+"("+tmpLength+"), not float"; 
											 }
											 units.add(tmpValue);		 
										 }							 
										 else{
											 if(isSynCorrect)  synErrMsg="Synthetic error near: "+thetoken.toString();isSynCorrect=false;
											 break;
										 }
									 }
									 thetoken=lexer.scan();
									 if(thetoken.toString().equals(","))thetoken=lexer.scan();
									 else if(thetoken.toString().equals(")"));
									 else{
										if(isSynCorrect)  synErrMsg="Synthetic error near: "+thetoken.toString();isSynCorrect=false;
										break;
									 }
								 }
								 if(isSemaCorrect&&i<CatalogManager.getTableAttriNum(tmpTableName)){
									 isSemaCorrect=false;
									 semaErrMsg="The number of values is smaller than that of attributes";
								 }
								 thetoken=lexer.scan();
								 if(isSynCorrect&&thetoken.toString().equals(";")){
									
									 
									 if(isSemaCorrect){
										 if(API.insertTuples(tmpTableName,new Tuple(units)))
										 	System.out.println("insert into "+tmpTableName+" succeeded.");
										 else
											 System.out.println("Error:insert into "+tmpTableName+" failed.");
									 }
									 else{
										 System.out.print(semaErrMsg);
										 System.out.println(", insert failed");
										 isSemaCorrect=true;
									 }
									
									
								 }
								 else{
									 if(isSynCorrect)  synErrMsg="Synthetic error near: "+thetoken.toString();isSynCorrect=false;
									 continue;
								 }	
							 }
							 else{
								 if(isSynCorrect)  synErrMsg="Synthetic error near: "+thetoken.toString();isSynCorrect=false;
								 continue;
							 }
						 }
						 else{
							 if(isSynCorrect)  synErrMsg="Synthetic error near: "+thetoken.toString();isSynCorrect=false;
							 continue;
						 }
					 }
					 else{
						 if(isSynCorrect)  synErrMsg="Synthetic error near: "+thetoken.toString();isSynCorrect=false;
						 continue;
					 }
				 }
				 else{
					 if(isSynCorrect)  synErrMsg="Synthetic error near: "+thetoken.toString();isSynCorrect=false;
					 continue;
				 }
			 }
			
			 else if(thetoken.tag==Tag.DELETE){
				 thetoken=lexer.scan();
				 if(thetoken.tag==Tag.FROM){
					 thetoken=lexer.scan();
					 if(thetoken.tag==Tag.ID){
						 String tmpTableName=thetoken.toString();
						 if(!CatalogManager.isTableExist(tmpTableName)){
							 semaErrMsg="The table "+tmpTableName+" doesn't exist";
							 isSemaCorrect=false;
						 }
						 thetoken=lexer.scan();
						 if(thetoken.tag==Tag.WHERE){
							 
							 ConditionNode tmpConditionNodes=ParsingCondition(lexer,tmpTableName,";");
							 if(thetoken.toString().equals(";")){
								
								 if(isSemaCorrect&&isSynCorrect){
									 
									 int deleteNum=API.deleteTuples(tmpTableName, tmpConditionNodes);									 
									 System.out.println("delete "+deleteNum+ " tuples from table "+tmpTableName);
									 
								 }
								 else if(!isSynCorrect){
									 continue;
								 }
								 else{
									 System.out.println(semaErrMsg+", delete tuples failed");
									 isSemaCorrect=true;
								 }
							 }
							 else{
								 if(isSynCorrect)  synErrMsg="Synthetic error near: "+thetoken.toString();isSynCorrect=false;
								 continue;
							 }
						 }
						 else if(thetoken.toString().equals(";")){
							 
							 if(isSemaCorrect){
								
								 int deleteNum=API.deleteTuples(tmpTableName, null);
								 
								 System.out.println("delete "+deleteNum+ " tuples from table "+tmpTableName);
							 
							 }
							 else{
								 System.out.println(semaErrMsg+", delete tuples failed");
								 isSemaCorrect=true;
							 }
						 }
						 else{
							 
							 if(isSynCorrect)  synErrMsg="Synthetic error near: "+thetoken.toString();isSynCorrect=false;
							 continue;
						 }
					 }
					 else{
						 if(isSynCorrect)  synErrMsg="Synthetic error near: "+thetoken.toString();isSynCorrect=false;
						 continue;
					 }
				 }
				 else{
					 if(isSynCorrect)  synErrMsg="Synthetic error near: "+thetoken.toString();isSynCorrect=false;
					 continue;
				 }					 
			 }
   			
			 else if(thetoken.tag==Tag.SELECT){
				 Vector<String>tmpAttriNames=ParsingProjection(lexer);
					 if(isSynCorrect&&thetoken.tag==Tag.FROM){
						 thetoken=lexer.scan();
						 if(thetoken.tag==Tag.ID){
							 String tmpTableName=thetoken.toString();
							 String tmpTableName2="";
							 boolean joinflag=false;
							 if(isSemaCorrect&&!CatalogManager.isTableExist(tmpTableName)){
								 semaErrMsg="The table "+tmpTableName+" doesn't exist";
								 isSemaCorrect=false;
							 }
							 if(tmpAttriNames!=null)
							 for(int i=0;i<tmpAttriNames.size();i++){
								 if(isSemaCorrect&&!CatalogManager.isAttributeExist(tmpTableName, tmpAttriNames.get(i))){
									 semaErrMsg="The attribute "+tmpAttriNames.get(i)+" doesn't exist";
									 isSemaCorrect=false;
								 }
							 }
							 thetoken=lexer.scan();
							 
							 if(thetoken.tag==Tag.JOIN||thetoken.toString().equals(",")){
								 joinflag=true;
								 thetoken=lexer.scan();
								 if(thetoken.tag==Tag.ID){
									 
									 tmpTableName2=thetoken.toString();
									 if(isSemaCorrect&&!CatalogManager.isTableExist(tmpTableName2)){
										 semaErrMsg="The table "+tmpTableName2+" doesn't exist";
										 isSemaCorrect=false;
									 }
									 thetoken=lexer.scan();
								 }
								 else{
									 if(isSynCorrect)  synErrMsg="Synthetic error near: "+thetoken.toString();isSynCorrect=false;
									 continue;
								 }
							 }
							 if(isSynCorrect&&thetoken.tag==Tag.WHERE){
								 
								 if(joinflag){
									 thetoken=lexer.scan();
									 String[]tmpName1=new String[2],tmpName2=new String[2];
							
									 if(thetoken.tag==Tag.ID){
										 tmpName1=thetoken.toString().split("\\.");
										   if(isSemaCorrect&&!CatalogManager.isTableExist(tmpName1[0])){
											   semaErrMsg="The table "+tmpName1[0]+" doesn't exist";
												isSemaCorrect=false;
										   }
										   if(isSemaCorrect&&!CatalogManager.isAttributeExist(tmpTableName, tmpName1[1])){
											   semaErrMsg="The attribute "+tmpName1[1]+" doesn't exist";
												isSemaCorrect=false;
										   }
										   thetoken=lexer.scan();
										   
										   if(thetoken.tag==Tag.OP){
											   thetoken=lexer.scan();
											   if(thetoken.tag==Tag.ID){
												   tmpName2=thetoken.toString().split("\\.");
												   if(isSemaCorrect&&!CatalogManager.isTableExist(tmpName2[0])){
													   semaErrMsg="The table "+tmpName2[0]+" doesn't exist";
														isSemaCorrect=false;
												   }
												   if(isSemaCorrect&&!CatalogManager.isAttributeExist(tmpTableName, tmpName2[1])){
													   semaErrMsg="The attribute "+tmpName2[1]+" doesn't exist";
														isSemaCorrect=false;
												   }
												   thetoken=lexer.scan();
												   if(thetoken.toString().equals(";")){
													   if(isSemaCorrect&&isSynCorrect){
															
														    for(int i=0;i<CatalogManager.getTableAttriNum(tmpTableName);i++){ 
																System.out.print("\t"+CatalogManager.getAttriName(tmpTableName, i));
															}
														    for(int i=0;i<CatalogManager.getTableAttriNum(tmpTableName2);i++){ 
																System.out.print("\t"+CatalogManager.getAttriName(tmpTableName2, i));
															}
														    System.out.println();
														    Vector<Tuple> seleteTuples=API.join(tmpName1[0],tmpName1[1],tmpName2[0],tmpName2[1]);
														    for(int i=0;i<seleteTuples.size();i++){
																System.out.println(seleteTuples.get(i).getString());
															}
															
														 }
														 else if(!isSynCorrect) continue;
														 else{
															 System.out.println(semaErrMsg+", select tuples failed");
															 isSemaCorrect=true;
														 }
												   }
												   else{
														 if(isSynCorrect)  synErrMsg="Synthetic error near: "+thetoken.toString();isSynCorrect=false;
														 continue;
													}
												   
											   }
											   else{
													 if(isSynCorrect)  synErrMsg="Synthetic error near: "+thetoken.toString();isSynCorrect=false;
													 continue;
												}
										   }
										   else{
												 if(isSynCorrect)  synErrMsg="Synthetic error near: "+thetoken.toString();isSynCorrect=false;
												 continue;
										   }
									 }
									 else{
										 if(isSynCorrect)  synErrMsg="Synthetic error near: "+thetoken.toString();isSynCorrect=false;
										 continue;
									 }
									 
									 continue;
								 }
								 ConditionNode tmpConditionNode=ParsingCondition(lexer,tmpTableName,";");
								 if(thetoken.toString().equals(";")){
									 if(isSemaCorrect&&isSynCorrect){
										 
										 showSelectRes(tmpTableName,tmpAttriNames, tmpConditionNode,null,false);
										
									 }
									 else if(!isSynCorrect) continue;
									 else{
										 System.out.println(semaErrMsg+", select tuples failed");
										 isSemaCorrect=true;
									 }
									
								 }
								 else if(isSynCorrect&&thetoken.tag==Tag.ORDER){
									 thetoken=lexer.scan();
									 if(thetoken.tag==Tag.BY){
										 thetoken=lexer.scan();
										 if(thetoken.tag==Tag.ID){
											 String tmpOrderAttriName=thetoken.toString();
											 if(isSemaCorrect&&!CatalogManager.isAttributeExist(tmpTableName, tmpOrderAttriName)){
												 semaErrMsg="The attribute "+tmpOrderAttriName+" doesn't exist";
												 isSemaCorrect=false;
											 }
											 thetoken=lexer.scan();
											 if(thetoken.toString().equals(";")||thetoken.tag==Tag.ASC||thetoken.tag==Tag.DESC){
												 boolean order;
												 if(thetoken.toString().equals(";")) order=true;
												 else {
													 order=thetoken.tag==Tag.ASC?true:false;
													 thetoken=lexer.scan();
													 if(isSynCorrect&&!thetoken.toString().equals(";")){
														 synErrMsg="Synthetic error near: "+thetoken.toString();
														 isSynCorrect=false;
														 continue;
													 }
												 }
												 if(isSemaCorrect){
												
													 showSelectRes(tmpTableName,tmpAttriNames, tmpConditionNode,tmpOrderAttriName,order);
													 
												 }
												 else{
													 System.out.println(semaErrMsg+", select tuples failed");
													 isSemaCorrect=true;
												 }										 
											 }
											 else{
												 if(isSynCorrect)  synErrMsg="Synthetic error near: "+thetoken.toString();isSynCorrect=false;
												 continue;
											 }
											 
										 } else{
											 if(isSynCorrect)  synErrMsg="Synthetic error near: "+thetoken.toString();isSynCorrect=false;
											 continue;
										 }
									 }
									 else{
										 if(isSynCorrect)  synErrMsg="Synthetic error near: "+thetoken.toString();isSynCorrect=false;
										 continue;
									 }
								 }
								 else{
									 if(isSynCorrect)  synErrMsg="Synthetic error near: "+thetoken.toString();isSynCorrect=false;
									 continue;
								 }
							 }
							 else if(thetoken.toString().equals(";")){
								 if(isSemaCorrect){
									 
									 showSelectRes(tmpTableName,tmpAttriNames, null,null,false);
								 }
								 else{
									 System.out.println(semaErrMsg+", select tuples failed");
									 isSemaCorrect=true;
								 }
							 }
							 else if(thetoken.tag==Tag.ORDER){
								 thetoken=lexer.scan();
								 if(thetoken.tag==Tag.BY){
									 thetoken=lexer.scan();
									 if(thetoken.tag==Tag.ID){
										 String tmpOrderAttriName=thetoken.toString();
										 if(isSemaCorrect&&!CatalogManager.isAttributeExist(tmpTableName, tmpOrderAttriName)){
											 semaErrMsg="The attribute "+tmpOrderAttriName+" doesn't exist";
											 isSemaCorrect=false;
										 }
										 thetoken=lexer.scan();
										 if(thetoken.toString().equals(";")||thetoken.tag==Tag.ASC||thetoken.tag==Tag.DESC){
											 boolean order;
											 if(thetoken.toString().equals(";")) order=true;
											 else {
												 order=thetoken.tag==Tag.ASC?true:false;
												 thetoken=lexer.scan();
												 if(isSynCorrect&&!thetoken.toString().equals(";")){
													 synErrMsg="Synthetic error near: "+thetoken.toString();
													 isSynCorrect=false;
													 continue;
												 }
											 }
											 if(isSemaCorrect){
												 
												 showSelectRes(tmpTableName,tmpAttriNames, null,tmpOrderAttriName,order);
												 
											 }
											 else{
												 System.out.println(semaErrMsg+", select tuples failed");
												 isSemaCorrect=true;
											 }										 
										 }
										 else{
											 if(isSynCorrect)  synErrMsg="Synthetic error near: "+thetoken.toString();isSynCorrect=false;
											 continue;
										 }
										 
									 } else{
										 if(isSynCorrect)  synErrMsg="Synthetic error near: "+thetoken.toString();isSynCorrect=false;
										 continue;
									 }
								 }
								 else{
									 if(isSynCorrect)  synErrMsg="Synthetic error near: "+thetoken.toString();isSynCorrect=false;
									 continue;
								 }
							 }
							 else{
								 
								 if(isSynCorrect)  synErrMsg="Synthetic error near: "+thetoken.toString();isSynCorrect=false;
								 continue;
							 }
						 }
						 else{
							 if(isSynCorrect)  synErrMsg="Synthetic error near: "+thetoken.toString();isSynCorrect=false;
							 continue;
						 }
					 }
					 else{
						 if(isSynCorrect)  synErrMsg="Synthetic error near: "+thetoken.toString();isSynCorrect=false;
						 continue;
					 }
				 					
				 		
			 }
			 else if(thetoken.tag==Tag.SHOW){
				 thetoken=lexer.scan();
				 if(thetoken.toString().equals("tables")){
					 thetoken=lexer.scan();
					 if(thetoken.toString().equals(";")){
						 API.showTableCatalog();
					 } else{
						 if(isSynCorrect)  synErrMsg="Synthetic error near: "+thetoken.toString();isSynCorrect=false;
						 continue;
					 }
					 
				 }
				 else if(thetoken.toString().equals("indexes")){
					 thetoken=lexer.scan();
					 if(thetoken.toString().equals(";")){
						 API.showIndexCatalog();
					 } else{
						 if(isSynCorrect)  synErrMsg="Synthetic error near: "+thetoken.toString();isSynCorrect=false;
						 continue;
					 }
				 }
				 else if(thetoken.toString().equals("catalog")){
					 thetoken=lexer.scan();
					 if(thetoken.toString().equals(";")){
						 API.showCatalog();
					 } else{
						 if(isSynCorrect)  synErrMsg="Synthetic error near: "+thetoken.toString();isSynCorrect=false;
						 continue;
					 }
				 }
				 else{
					 if(isSynCorrect)  synErrMsg="Synthetic error near: "+thetoken.toString();isSynCorrect=false;
					 continue;
				 }
			 }
			 else{
				 if(isSynCorrect)  synErrMsg="Synthetic error near: "+thetoken.toString();isSynCorrect=false;
				 continue;
			 }
   			 
   		} 
		   
   		 
   	}
 
private static void showSelectRes(String tmpTableName,Vector<String> tmpAttriNames,ConditionNode tmpConditionNode,String tmpOrderAttriName,boolean order){
		if(tmpAttriNames==null)
			for(int i=0;i<CatalogManager.getTableAttriNum(tmpTableName);i++){ 
				System.out.print("\t"+CatalogManager.getAttriName(tmpTableName, i));
			}
		else
			for(int i=0;i<tmpAttriNames.size();i++)
				System.out.print("\t"+tmpAttriNames.get(i));
		System.out.println();
		Vector<Tuple> seleteTuples;
		if(tmpOrderAttriName==null)
			seleteTuples=API.selectTuples(tmpTableName,tmpAttriNames, tmpConditionNode);		
		else{
			seleteTuples=API.selectTuples(tmpTableName,tmpAttriNames, tmpConditionNode,tmpOrderAttriName,order);
		}
		for(int i=0;i<seleteTuples.size();i++){
			System.out.println(seleteTuples.get(i).getString());
		}
		System.out.println("There are "+seleteTuples.size()+" tuples returned");
   }

private static Vector<String> ParsingProjection(Lexer lexer) throws IOException{
	   Vector<String>tmpAttriNames=new Vector<String>();
	   thetoken=lexer.scan();
	   if(thetoken.toString().equals("*")){
		   thetoken=lexer.scan();
		   return null;
	   }
	   else{
		   while(thetoken.tag!=Tag.FROM){
			   if(thetoken.tag==Tag.ID){
				   tmpAttriNames.add(thetoken.toString());
				   thetoken=lexer.scan();
				   if(thetoken.toString().equals(",")){
					   thetoken=lexer.scan();
				   }
				   else if(thetoken.tag==Tag.FROM);
				   else{
					   if(isSynCorrect)  synErrMsg="Synthetic error near: "+thetoken.toString();isSynCorrect=false;
						 break;
				   }
			   }
			   else{
				   if(isSynCorrect)  synErrMsg="Synthetic error near: "+thetoken.toString();isSynCorrect=false;
					 break;
			   }
			   
		   }
		   return tmpAttriNames;
	   }
   }

private static ConditionNode ParsingExpression(Lexer lexer,String tmpTableName) throws IOException{
	   String tmpAttriName;Comparison op;String tmpValue;	
	   	boolean constantFlag = false;
	   if(thetoken.tag==Tag.ID){
		   tmpAttriName=thetoken.toString();
		   if(isSemaCorrect&&!CatalogManager.isAttributeExist(tmpTableName, tmpAttriName)){
			   isSemaCorrect=false;
				 semaErrMsg="The attribute "+tmpAttriName+" doesn't exist";
		   }
		   thetoken=lexer.scan();
		   if(thetoken.tag==Tag.OP){
			   op=Comparison.parseCompar(thetoken);
			   thetoken=lexer.scan();
			   tmpValue=thetoken.toString();
			   if(isSemaCorrect){
				   if(thetoken.tag==Tag.STR){
					   constantFlag=true;
					   String tmpType=CatalogManager.getType(tmpTableName, tmpAttriName);
					   int tmpLength=CatalogManager.getLength(tmpTableName, tmpAttriName);
					 
					   if(!tmpType.equals("char")
								 ||tmpLength<tmpValue.getBytes().length){
							 isSemaCorrect=false;
							 semaErrMsg="The type of value +"+tmpValue+" should be "+tmpType+"("+tmpLength+"), not char("+tmpValue.getBytes().length+")";
					   }
				   }
				   else if(thetoken.tag==Tag.INTNUM){
					   constantFlag=true;
					   String tmpType=CatalogManager.getType(tmpTableName, tmpAttriName);
					   int tmpLength=CatalogManager.getLength(tmpTableName, tmpAttriName);
					   
						 if(!tmpType.toString().equals("int")
								 &&!tmpType.equals("float")){
							isSemaCorrect=false;
							 semaErrMsg="The type of value +"+tmpValue+" should be "+tmpType+"("+tmpLength+"), not int";
						 }
				   }
				   else if(thetoken.tag==Tag.FLOATNUM){
					   constantFlag=true;
					   String tmpType=CatalogManager.getType(tmpTableName, tmpAttriName);
					   int tmpLength=CatalogManager.getLength(tmpTableName, tmpAttriName);
					   
					   if(!tmpType.equals("float")){
							isSemaCorrect=false;
							 semaErrMsg="The type of value +"+tmpValue+" should be "+tmpType+"("+tmpLength+"), not float"; 
						}
				   }
				   else if(thetoken.tag==Tag.ID){
					   constantFlag=false;
					   String tmpType1=CatalogManager.getType(tmpTableName, tmpAttriName);
					   String tmpType2=CatalogManager.getType(tmpTableName, tmpValue);
					   
					   if(!tmpType1.equals(tmpType2)){
						   isSemaCorrect=false;
							 semaErrMsg="The two attributes are in different types and cannot be compared"; 
					   }
				   }
				   else{
						if(isSynCorrect)  synErrMsg="Synthetic error near: "+thetoken.toString();
						isSynCorrect=false;
						
				   }				   
				   
				    
			   }
			   return new ConditionNode(tmpAttriName,op,tmpValue,constantFlag);
			   
		   }
		   else{
				if(isSynCorrect)  synErrMsg="Synthetic error near: "+thetoken.toString();
				isSynCorrect=false;
		   }
	   }
	   else{
		   if(isSynCorrect)  synErrMsg="Synthetic error near: "+thetoken.toString();
			isSynCorrect=false; 
	   }
	return null;	   
   }

private static ConditionNode ParsingCondition(Lexer lexer,String tmpTableName,String endtoken)throws IOException {
	  
	   ConditionNode tmpConditionRoot = null;
	   ConditionNode tmpExpresstion = null,tmpConjunction;
	   thetoken=lexer.scan();
	   boolean flag=false;
	   if(thetoken.toString().equals("(")){
		   tmpConditionRoot=ParsingCondition(lexer,tmpTableName,")");
		   flag=true;
	   }
	   else if(thetoken.tag==Tag.ID){
		   tmpConditionRoot=ParsingExpression(lexer,tmpTableName);
	   }
	   else{
		   if(isSynCorrect)  synErrMsg="Synthetic error near: "+thetoken.toString();
			isSynCorrect=false; 
	   }
	   if(tmpConditionRoot==null||!isSynCorrect){
		   return null;
	   }
	 
	   thetoken=lexer.scan();	
	   while(!thetoken.toString().equals(endtoken)&&thetoken.tag!=Tag.ORDER){
		   if(thetoken.tag==Tag.AND){
			   tmpConjunction=new ConditionNode("and");
			   thetoken=lexer.scan();
			   if(thetoken.toString().equals("(")){
				   tmpExpresstion=ParsingCondition(lexer,tmpTableName,")");
			   }
			   else if(thetoken.tag==Tag.ID){
				   tmpExpresstion=ParsingExpression(lexer,tmpTableName);
			   }
			   else{
				   if(isSynCorrect)  synErrMsg="Synthetic error near: "+thetoken.toString();
					isSynCorrect=false; 
			   }
			   if(tmpExpresstion==null){
				   return null;
			   }
			   
			   if(tmpConditionRoot.conjunction=="or"&&flag==false){
				   
				   tmpConditionRoot=tmpConditionRoot.linkChildNode(tmpConditionRoot.left, tmpConjunction.linkChildNode(tmpConditionRoot.right, tmpExpresstion));   
				   
			   }
			   
			   else{
				   tmpConditionRoot=tmpConjunction.linkChildNode(tmpConditionRoot, tmpExpresstion);
				   if(flag) flag=false;
			   }
				   
			  
				   
		   }
		   else if(thetoken.tag==Tag.OR){
			   tmpConjunction=new ConditionNode("or");
			   thetoken=lexer.scan();
			   if(thetoken.toString().equals("(")){
				   tmpExpresstion=ParsingCondition(lexer,tmpTableName,")");
			   }
			   else if(thetoken.tag==Tag.ID){
				   tmpExpresstion=ParsingExpression(lexer,tmpTableName);
			   }
			   else{
				   if(isSynCorrect)  synErrMsg="Synthetic error near: "+thetoken.toString();
					isSynCorrect=false; 
			   }
			  
			   if(tmpExpresstion==null){
				   return null;
			   }
			 
			   tmpConditionRoot=tmpConjunction.linkChildNode(tmpConditionRoot, tmpExpresstion);
		   }
		   
		   else if(thetoken.toString().equals(endtoken)||thetoken.tag==Tag.ORDER);
		   else{
				if(isSynCorrect)  synErrMsg="Synthetic error near: "+thetoken.toString();isSynCorrect=false;
				break;
		   }
			thetoken=lexer.scan(); 
			
	   }
	 
	return tmpConditionRoot;
	
   }

}