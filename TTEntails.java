import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;

public class TTEntailsAlgorithm {
	Set<String> symbolList = new HashSet<String>();	
	int counter = 0;
	
	public boolean ttEntails(LogicalExpression kb_plus_rules, Model m, LogicalExpression alpha) {
		List<String> symbolList = getSymbols(kb_plus_rules, alpha);
		symbolList = removeSymbols(m,symbolList);
		return ttCheckAll(kb_plus_rules, alpha, symbolList, m);
	}
	
	private List<String> removeSymbols(Model m, List<String> symbolList2) {
		
		Iterator<Entry<String,Boolean>> it = m.h.entrySet().iterator();
	    while (it.hasNext()) {	    	
	        Entry<String,Boolean> pair = (Entry<String,Boolean>)it.next();
	        symbolList2.remove(pair.getKey());	       
	    }
		return symbolList2;
	}

	boolean pl_true(LogicalExpression kb, Model m){

		if(isLeaf(kb)){						
			return m.h.get(kb.getUniqueSymbol());			
		}
		else if(kb.getConnective()!=null && kb.getConnective().equalsIgnoreCase("not")){			
			return !(pl_true(kb.getNextSubexpression(),m));
		}
		else if(kb.getConnective()!=null && kb.getConnective().equalsIgnoreCase("or")){			
			Vector<LogicalExpression> vector = kb.getSubexpressions();
			Boolean b = false;
			for(int i=0;i<vector.size();i++){
				b = b || pl_true(vector.get(i),m);
			}
			return b;		
		}
		else if(kb.getConnective()!=null && kb.getConnective().equalsIgnoreCase("if")){		
			
			Vector<LogicalExpression> vector = kb.getSubexpressions();
			Boolean b = pl_true(vector.get(0),m);			
			b = !(b && !(pl_true(vector.get(1),m)));
			return b;			
		}
		else if(kb.getConnective()!=null && kb.getConnective().equalsIgnoreCase("iff")){			
			Vector<LogicalExpression> vector = kb.getSubexpressions();
			Boolean b = pl_true(vector.get(0),m);			
			return b == pl_true(vector.get(1),m);
		}
		else if(kb.getConnective()!=null && kb.getConnective().equalsIgnoreCase("and")){			
			Vector<LogicalExpression> vector = kb.getSubexpressions();
			
			Boolean b = true;
			for(int i=0;i<vector.size();i++){				
				b = b && pl_true(vector.get(i),m);
				if(b==false){	
					return b;
				}
			}
			return b;
		}
		else if(kb.getConnective()!=null && kb.getConnective().equalsIgnoreCase("xor")){			
			Vector<LogicalExpression> vector = kb.getSubexpressions();
			Boolean b = false;
			int truthCounter=0;
			for(int i=0;i<vector.size();i++){
				boolean retrieved = pl_true(vector.get(i),m);
				if(retrieved==true)truthCounter++;
				if(truthCounter>1)return false;
				b = ((b||retrieved) && !(b && retrieved));
			}
			return b;
		}
		return true;
	}
	
	boolean isLeaf(LogicalExpression kb){
		return kb.getConnective()==null;
	}

	public boolean ttCheckAll(LogicalExpression kb, LogicalExpression alpha,	List<String> symbols, Model model) {		
		if (symbols.isEmpty()) {			
			boolean pl_true = pl_true(kb, model);			
			if(pl_true){
				return pl_true(alpha, model);				
			}
			else{
				return true;
			}			
		} else {
			String P = (String) symbols.get(0);			
			List<String> rest = symbols.subList(1, symbols.size());			
			Model trueModel = model.extend(P, true);
			Model falseModel = model.extend(P,false);
			return (ttCheckAll(kb, alpha, rest, trueModel) && (ttCheckAll(kb, alpha, rest, falseModel)));
		}		
	}
	
	List<String> getSymbols(LogicalExpression kb, LogicalExpression alpha){		
		getSymbols(kb);
		getSymbols(alpha);		
		List<String> returnList = new ArrayList<String>(symbolList);
		return returnList;
	}
	
	void getSymbols(LogicalExpression le){
		if(le.getUniqueSymbol()!=null)symbolList.add(le.getUniqueSymbol());
		else
		for(int i=0;i<le.getSubexpressions().size();i++){
			LogicalExpression lle = (LogicalExpression) le.getSubexpressions().get(i);
			getSymbols(lle);
			if(lle.getUniqueSymbol()!=null) symbolList.add(lle.getUniqueSymbol());			
		}		
	}		

	class Model{

		public HashMap<String,Boolean> h = new HashMap<String,Boolean>();

			public Model extend(String symbol, boolean b) {
			Model m = new Model();
			m.h.putAll(this.h);
			m.h.put(symbol, b);
			return m;
		}
		
	}
}
