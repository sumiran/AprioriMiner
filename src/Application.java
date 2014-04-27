import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;


public class Application {
	
	static class Rule {
		public ArrayList<String> LHS;
		public ArrayList<String> RHS;
		public double supportFound;
		public double confidenceFound;
		
		public Rule() {
			LHS = new ArrayList<String>();
			RHS = new ArrayList<String>();
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
		
		public double findSupport(ArrayList<String> items) {
			return (100.0*findCount(items))/rows.size();
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
			
			return (100.0*RHSAndLHSCount)/LHSCount;
		}
		
	}
	
	public static ArrayList<Rule> aprioriMine(DataSet dataSet) {
		ArrayList<Rule> rules = new ArrayList<Rule>();
		
		
		return rules;
	}
	
	public static DataSet dataSetFromFile(String path) {
		DataSet dataSet = new DataSet();
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(path));
			String line;
			while ((line = br.readLine()) != null) {
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
	
	public static void main(String args[]) {
		String[] myArg = {"MyFile.csv", "0.3", "0.5"};
		args = myArg;
		
		String filePath = args[0];
		double minSupport = Double.parseDouble(args[1]);
		double minConfidence = Double.parseDouble(args[2]);
		
		System.out.println("Reading file: 				      "+filePath);
		System.out.println("Finding rules with minSupport:    "+minSupport);
		System.out.println("                   minConfidence: "+minConfidence);
		
		DataSet myDataSet = dataSetFromFile(filePath);
		
		System.out.println("Mining frequent itemsets and rules...");
		ArrayList<Rule> rulesFound = aprioriMine(myDataSet);
		
		System.out.println("\nFound the following rules: ");
		for(Rule r : rulesFound) {
			System.out.println(r.printToString());
		}
	}

}
