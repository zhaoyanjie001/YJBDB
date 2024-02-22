
package recmgr;
import com.yjb.core.bufmgr.*;
import com.yjb.core.catmgr.*;
import com.yjb.core.filmgr.*;
import com.yjb.core.recmgr.*;
import parse.Comparison;

public class conditionNode {

	String tablename;
	public String attriName;
	String tablename2;
	String attriName2;
	public String conjunction;
	public Comparison op;
	public String value;
	public conditionNode left;
	public conditionNode right;
	boolean constantFlag;
		

	public String toString(){
		return attriName+" "+op+" "+value;
	}
	
	public conditionNode(String attriName, Comparison op, String value,boolean constantFlag) {
		this.conjunction="";
		this.attriName = attriName;
		this.op = op;
		this.left=null;
		this.right=null;
		this.constantFlag=constantFlag;
		if(constantFlag){
			this.value = value;
		}
		else{
			this.attriName2=value;
		}
	}

	public conditionNode(String attriName, String op, String value) {
		this.attriName = attriName;
		this.conjunction="";
		this.op = Comparison.parseCompar(op);		
		this.left=null;
		this.right=null;		
		this.constantFlag=true;
		this.value = value;

	}

	public conditionNode(String conjunction) {
		this.attriName = "";
		this.op = null;
		this.value = "";
		this.conjunction=conjunction;
	}
	
	public conditionNode linkChildNode( conditionNode l, conditionNode r) {
		this.left=l;
		this.right=r;
		return this;
	}

	public boolean calc(String tablename, tuple T) {
		if (conjunction.equals("and"))
			return (left.calc(tablename, T) & right.calc(tablename, T));
		else if (conjunction.equals("or"))
			return (left.calc(tablename, T) | right.calc(tablename, T));
		else {
			if (op == Comparison.eq) {
				if (CatalogManager.getType(tablename, attriName).equals("int")) {
					int num1 = Integer.parseInt(T.units.elementAt(CatalogManager.getAttriOffest(tablename, attriName)));
					int num2;
					if (constantFlag)
						num2 = Integer.parseInt(value);
					else 
						num2 = Integer.parseInt(T.units.elementAt(CatalogManager.getAttriOffest(tablename, attriName2))); 
					if (num1 != num2)
						return false;
				}
				if (CatalogManager.getType(tablename, attriName)
						.equals("float")) {					
					float num1 = Float.parseFloat(T.units
							.elementAt(CatalogManager.getAttriOffest(tablename, attriName)));
					float num2 ;
					if (constantFlag)
						num2 = Float.parseFloat(value);
					else 
						num2 = Float.parseFloat(T.units
								.elementAt(CatalogManager.getAttriOffest(tablename, attriName2)));
					if (num1 != num2)
						return false;
				}
				if (CatalogManager.getType(tablename, attriName).equals("char")) {
					String num1 = T.units.elementAt(CatalogManager.getAttriOffest(tablename, attriName));
					String num2;
					if (constantFlag)
						num2 = value;
					else
						num2 = T.units.elementAt(CatalogManager.getAttriOffest(tablename, attriName2));
					if (!num1.equals(num2))
						return false;
				}
			} else if (op == Comparison.ne) {
				if (CatalogManager.getType(tablename, attriName).equals("int")) {
					int num1 = Integer.parseInt(T.units.elementAt(CatalogManager.getAttriOffest(tablename, attriName)));
					int num2;
					if (constantFlag)
						num2 = Integer.parseInt(value);
					else 
						num2 = Integer.parseInt(T.units.elementAt(CatalogManager.getAttriOffest(tablename, attriName2)));
					if (num1 == num2)
						return false;
				}
				if (CatalogManager.getType(tablename, attriName)
						.equals("float")) {
					float num1 = Float.parseFloat(T.units
							.elementAt(CatalogManager.getAttriOffest(tablename, attriName)));
					float num2 ;
					if (constantFlag)
						num2 = Float.parseFloat(value);
					else 
						num2 = Float.parseFloat(T.units
								.elementAt(CatalogManager.getAttriOffest(tablename, attriName2)));
					if (num1 == num2)
						return false;
				}
				if (CatalogManager.getType(tablename, attriName).equals("char")) {
					String num1 = T.units.elementAt(CatalogManager.getAttriOffest(tablename, attriName));
					String num2;
					if (constantFlag)
						num2 = value;
					else
						num2 = T.units.elementAt(CatalogManager.getAttriOffest(tablename, attriName2));
					if (num1.equals(num2))
						return false;
				}
			} else if (op == Comparison.lt) {
				if (CatalogManager.getType(tablename, attriName).equals("int")) {
					int num1 = Integer.parseInt(T.units.elementAt(CatalogManager.getAttriOffest(tablename, attriName)));
					int num2;
					if (constantFlag)
						num2 = Integer.parseInt(value);
					else 
						num2 = Integer.parseInt(T.units.elementAt(CatalogManager.getAttriOffest(tablename, attriName2)));
					if (num1 >= num2)
						return false;
				}
				if (CatalogManager.getType(tablename, attriName)
						.equals("float")) {
					float num1 = Float.parseFloat(T.units
							.elementAt(CatalogManager.getAttriOffest(tablename, attriName)));
					float num2 ;
					if (constantFlag)
						num2 = Float.parseFloat(value);
					else 
						num2 = Float.parseFloat(T.units
								.elementAt(CatalogManager.getAttriOffest(tablename, attriName2)));
					if (num1 >= num2)
						return false;
				}
				if (CatalogManager.getType(tablename, attriName).equals("char")) {
					String num1 = T.units.elementAt(CatalogManager.getAttriOffest(tablename, attriName));
					String num2;
					if (constantFlag)
						num2 = value;
					else
						num2 = T.units.elementAt(CatalogManager.getAttriOffest(tablename, attriName2));
					if (num1.compareTo(num2) >= 0)
						return false;
				}
			} else if (op == Comparison.le) {
				if (CatalogManager.getType(tablename, attriName).equals("int")) {
					int num1 = Integer.parseInt(T.units.elementAt(CatalogManager.getAttriOffest(tablename, attriName)));
					int num2;
					if (constantFlag)
						num2 = Integer.parseInt(value);
					else 
						num2 = Integer.parseInt(T.units.elementAt(CatalogManager.getAttriOffest(tablename, attriName2)));
					if (num1 > num2)
						return false;
				}
				if (CatalogManager.getType(tablename, attriName)
						.equals("float")) {
					float num1 = Float.parseFloat(T.units
							.elementAt(CatalogManager.getAttriOffest(tablename, attriName)));
					float num2 ;
					if (constantFlag)
						num2 = Float.parseFloat(value);
					else 
						num2 = Float.parseFloat(T.units
								.elementAt(CatalogManager.getAttriOffest(tablename, attriName2)));
					if (num1 > num2)
						return false;
				}
				if (CatalogManager.getType(tablename, attriName).equals("char")) {
					String num1 = T.units.elementAt(CatalogManager.getAttriOffest(tablename, attriName));
					String num2;
					if (constantFlag)
						num2 = value;
					else
						num2 = T.units.elementAt(CatalogManager.getAttriOffest(tablename, attriName2));
					if (num1.compareTo(num2) > 0)
						return false;
				}
			} else if (op == Comparison.gt) {
				if (CatalogManager.getType(tablename, attriName).equals("int")) {
					int num1 = Integer.parseInt(T.units.elementAt(CatalogManager.getAttriOffest(tablename, attriName)));
					int num2;
					if (constantFlag)
						num2 = Integer.parseInt(value);
					else 
						num2 = Integer.parseInt(T.units.elementAt(CatalogManager.getAttriOffest(tablename, attriName2)));
					if (num1 <= num2)
						return false;
				}
				if (CatalogManager.getType(tablename, attriName)
						.equals("float")) {
					float num1 = Float.parseFloat(T.units
							.elementAt(CatalogManager.getAttriOffest(tablename, attriName)));
					float num2 ;
					if (constantFlag)
						num2 = Float.parseFloat(value);
					else 
						num2 = Float.parseFloat(T.units
								.elementAt(CatalogManager.getAttriOffest(tablename, attriName2)));
					if (num1 <= num2)
						return false;
				}
				if (CatalogManager.getType(tablename, attriName).equals("char")) {
					String num1 = T.units.elementAt(CatalogManager.getAttriOffest(tablename, attriName));
					String num2;
					if (constantFlag)
						num2 = value;
					else
						num2 = T.units.elementAt(CatalogManager.getAttriOffest(tablename, attriName2));
					if (num1.compareTo(num2) <= 0)
						return false;
				}
			} else if (op == Comparison.ge) {
				if (CatalogManager.getType(tablename, attriName).equals("int")) {
					int num1 = Integer.parseInt(T.units.elementAt(CatalogManager.getAttriOffest(tablename, attriName)));
					int num2;
					if (constantFlag)
						num2 = Integer.parseInt(value);
					else 
						num2 = Integer.parseInt(T.units.elementAt(CatalogManager.getAttriOffest(tablename, attriName2)));
					if (num1 < num2)
						return false;
				}
				if (CatalogManager.getType(tablename, attriName)
						.equals("float")) {
					float num1 = Float.parseFloat(T.units
							.elementAt(CatalogManager.getAttriOffest(tablename, attriName)));
					float num2 ;
					if (constantFlag)
						num2 = Float.parseFloat(value);
					else 
						num2 = Float.parseFloat(T.units
								.elementAt(CatalogManager.getAttriOffest(tablename, attriName2)));
					if (num1 < num2)
						return false;
				}
				if (CatalogManager.getType(tablename, attriName).equals("char")) {
					String num1 = T.units.elementAt(CatalogManager.getAttriOffest(tablename, attriName));
					String num2;
					if (constantFlag)
						num2 = value;
					else
						num2 = T.units.elementAt(CatalogManager.getAttriOffest(tablename, attriName2));
					if (num1.compareTo(num2) < 0)
						return false;
				}
			}
			return true;
		}

	}
	
}