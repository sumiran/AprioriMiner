import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;


public class Application {
	
	public static String printArrayListToString(ArrayList<String> line) {
		StringBuilder print = new StringBuilder();
		for(String item : line) {
			print.append("["+item+"] ");
		}
		return print.toString();
	}
	
	public static String printMultipleArrayListsToString(ArrayList<ArrayList<String>> rows) {
		StringBuilder print = new StringBuilder();
		
		for(ArrayList<String> itemSet : rows) {
			print.append(printArrayListToString(itemSet));
			print.append("\n");
		}
		
		
		return print.toString();
	}
	
	static class Rule {
		public ArrayList<String> LHS;
		public ArrayList<String> RHS;
		public double supportFound;
		public double confidenceFound;
		
		public Rule() {
			LHS = new ArrayList<String>();
			RHS = new ArrayList<String>();
			supportFound = -1.0;
			confidenceFound = -1.0;
		}
		
		public String printToString() {
			StringBuilder print = new StringBuilder();
			
			for(int i = 0; i < LHS.size(); i++) {
				print.append("["+LHS.get(i)+"]");
				if(i != LHS.size() - 1) {
					print.append(", ");
				}
			}
			
			print.append(" => ");
			
			for(int i = 0; i < RHS.size(); i++) {
				print.append("["+RHS.get(i)+"]");
				if(i != RHS.size() - 1) {
					print.append(", ");
				}
			}
			
			return print.toString();
		}
	}
	
	static class DataSet {
		public ArrayList<ArrayList<String>> rows;
		
		public DataSet() {
			rows = new ArrayList<ArrayList<String>>();
		}
		
		public String printToString() {
			return printMultipleArrayListsToString(rows);
		}
		
		public int findCount(ArrayList<String> items) {
			int count = 0;
			
			for(ArrayList<String> row : rows) {
				HashSet<String> rowItems = new HashSet<String>();
				
				for(String item : row) {
					rowItems.add(item);
				}
				
				boolean allFound = true;
				
				for(String item : items) {
					if(!rowItems.contains(item)) {
						allFound = false;
						break;
					}
				}
				
				if(allFound) {
					count += 1;
				}
			}
			
			return count;
		}
		
		public int findCount(String item) {
			ArrayList<String> wrapper = new ArrayList<String>();
			wrapper.add(item);
			return findCount(wrapper);
		}
		
		public double findSupport(ArrayList<String> items) {
			return (1.0*findCount(items))/rows.size();
		}
		
		public double findSupport(String item) {
			return (1.0*findCount(item))/rows.size();
		}
		
		public double findConfidence(Rule rule) {
			int LHSCount = 0;
			int RHSAndLHSCount = 0;
			
			ArrayList<String> LHSAndRHS = new ArrayList<String>();
			
			for(String i : rule.LHS) {
				LHSAndRHS.add(i);
			}
			
			for(String i : rule.RHS) {
				LHSAndRHS.add(i);
			}
			
			LHSCount = findCount(rule.LHS);
			RHSAndLHSCount = findCount(rule.RHS);
			
			return (1.0*RHSAndLHSCount)/LHSCount;
		}
		
	}
	
	public static DataSet dataSetFromFile(String path) {
		DataSet dataSet = new DataSet();
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(path));
			String line;
			while ((line = br.readLine()) != null) {
				
				if(line.trim().equals("")) {
					continue;
				}
				
				String[] lineItems = line.split(",");
				   
				ArrayList<String> dataSetRow = new ArrayList<String>();
				 
				for(String item : lineItems) {
					dataSetRow.add(item.trim());
				}
				   
				dataSet.rows.add(dataSetRow);
			}
			br.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return dataSet;
	}
	
	public static boolean itemsMatch(String item1, String item2) {
		return (item1.trim().equalsIgnoreCase(item2.trim()));
	}
	
	public static boolean canCombineItemsets(ArrayList<String> is1, ArrayList<String> is2) {
		if(is1.size() != is2.size()) {
			return false;
		}
		
		int commonElements = 0;
		
		for(String item : is2) {
			if(is1.contains(item)) {
				commonElements += 1;
			}
		}
		
		return (commonElements == is1.size() - 1);
	}
	
	public static ArrayList<String> combineItemsets(ArrayList<String> itemSet1, ArrayList<String> itemSet2) {
		
		if(!canCombineItemsets(itemSet1, itemSet2)) {
			return null;
		}
		
		ArrayList<String> combined = new ArrayList<String>();
		for(String item : itemSet1) {
			if(!combined.contains(item)) {
				combined.add(item);
			}
		}
		for(String item : itemSet2) {
			if(!combined.contains(item)) {
				combined.add(item);
			}
		}
		
		Collections.sort(combined);
		return combined;
	}
	
	public static boolean isSameItemset(ArrayList<String> set1, ArrayList<String> set2) {
		if(set1.size() != set2.size()) {
			return false;
		}
		
		Collections.sort(set1);
		Collections.sort(set2);
		
		for(int i=0;i<set1.size();i++) {
			if(!set1.get(i).equals(set2.get(i))) {
				return false;
			}
		}
		
		return true;
	}
	
	public static boolean containsItemSet(ArrayList<ArrayList<String>> mainSet, ArrayList<String> newItemset) {
		if(mainSet.size() == 0) {
			return false;
		}
		for(ArrayList<String> i : mainSet) {
			if(isSameItemset(i, newItemset)) {
				return true;
			}
		}
		return false;
	}
	
	public static ArrayList<ArrayList<String>> c_KPlusOneFromL_K(ArrayList<ArrayList<String>> itemSets) {
		ArrayList<ArrayList<String>> combinations = new ArrayList<ArrayList<String>>();
		for(int i=0;i<itemSets.size() - 1;i++) {
			for(int j=i+1;j<itemSets.size();j++) {
				ArrayList<String> combo = combineItemsets(itemSets.get(i), itemSets.get(j)); 
				if(combo != null) {
					if(!containsItemSet(combinations, combo)) {
						combinations.add(combo);
					}
				}
			}
		}
		return combinations;
	}
	
	public static ArrayList<Rule> aprioriMine(DataSet dataSet, double minSupport, double minConfidence) {
		ArrayList<Rule> rules = new ArrayList<Rule>();
		
		int k = 1;
		ArrayList<ArrayList<String>> large_k_itemsets = new ArrayList<ArrayList<String>>();
		HashSet<String> uniqueItems = new HashSet<String>();
		
		for(ArrayList<String> row : dataSet.rows) {
			for(String item : row) {
				if(!uniqueItems.contains(item)) {
					uniqueItems.add(item);
				}
			}
		}
		
		for(String item : uniqueItems) {
			if(dataSet.findSupport(item) >= minSupport) {
				ArrayList<String> initial = new ArrayList<String>();
				initial.add(item);
				large_k_itemsets.add(initial);
			}
		}
		
		//printMultipleArrayListsToString(large_k_itemsets);
		
		while(true) {
			ArrayList<ArrayList<String>> newItemsets = new ArrayList<ArrayList<String>>();
			
			ArrayList<ArrayList<String>> candidateSets = c_KPlusOneFromL_K(large_k_itemsets);
			for(ArrayList<String> itemSet : candidateSets) {
				if(dataSet.findSupport(itemSet) >= minSupport) {
					newItemsets.add(itemSet);
				}
			}
			
			if(newItemsets.size() == 0) {
				break;
			}
			
			k += 1;
			large_k_itemsets = newItemsets;
		}
		
		System.out.println(printMultipleArrayListsToString(large_k_itemsets));
		
		return rules;
	}
	
	public static void main(String args[]) {
		String[] myArg = {"D:\\EclipseWorkspace\\AprioriMiner\\INTEGRATED-DATASET.csv", "0.1", "0.5"};
		args = myArg;
		
		String filePath;
		double minSupport;
		double minConfidence;
		
		try {
			filePath = args[0];
			minSupport = Double.parseDouble(args[1]);
			minConfidence = Double.parseDouble(args[2]);
		} catch(Exception e) {
			System.out.println("Unable to parse arguments: ");
			e.printStackTrace();
			return;
		}
		
		System.out.println("Reading file: 				      "+filePath);
		System.out.println("Finding rules with minSupport:    "+minSupport);
		System.out.println("                   minConfidence: "+minConfidence);
		
		DataSet myDataSet = dataSetFromFile(filePath);
		
		//System.out.println(myDataSet.printToString());
		
		System.out.println("Mining frequent itemsets and rules...");
		ArrayList<Rule> rulesFound = aprioriMine(myDataSet, minSupport, minConfidence);
		
		System.out.println("\nFound the following "+rulesFound.size()+" rules: ");
		for(Rule r : rulesFound) {
			System.out.println(r.printToString());
		}
	}

}
