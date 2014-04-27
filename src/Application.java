import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;


public class Application {
	
	//Simple wrapper class for a rule. All items are represented as Strings in our program. Thus all rows/itemsets are ArrayList<String>
	static class Rule implements Comparable<Rule> {
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
			return LHS+" => "+RHS+" (Conf: "+confidenceFound+", Supp:"+supportFound+")";
		}

		public int compareTo(Rule r) {
			int val = (int)(100*(r.supportFound - supportFound));
			if(val == 0) {
				val = (int)(100*(r.confidenceFound - confidenceFound));
			}
			return val;
		}
	}
	
	//Simple wrapper class to hold the parent dataset in memory. Uses an ArrayList<ArrayList<String>> to hold all rows
	static class DataSet {
		public ArrayList<ArrayList<String>> rows;
		
		public DataSet() {
			rows = new ArrayList<ArrayList<String>>();
		}
		
		//Used to find support of 'items'. It returns the number of rows in the dataset which contain all elements contained in the list of items passed to it
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
		
		//Similar to the above function, overloaded to handle single items. Useful for the first apriori iteration.
		public int findCount(String item) {
			ArrayList<String> wrapper = new ArrayList<String>();
			wrapper.add(item);
			return findCount(wrapper);
		}
		
		//Normalizes the support to the number of rows
		public double findSupport(ArrayList<String> items) {
			return (1.0*findCount(items))/rows.size();
		}
		
		//Similar to above. Overloaded to handle a single item
		public double findSupport(String item) {
			return (1.0*findCount(item))/rows.size();
		}
		
		//Finds the confidence of a rule on this dataset
		public double findConfidence(Rule rule) {
			int LHSCount = 0;
			int LHSAndRHSCount = 0;
			
			ArrayList<String> LHSAndRHS = new ArrayList<String>();
			
			LHSAndRHS.addAll(rule.LHS);
			LHSAndRHS.addAll(rule.RHS);
			
			LHSCount = findCount(rule.LHS);
			LHSAndRHSCount = findCount(LHSAndRHS);
			
			return (1.0*LHSAndRHSCount)/LHSCount;
		}
		
	}
	
	//Reads a csv file and constructs a dataset.
	//Commas inside fields surrounded by quotes are not supported so the file needs to be formatted as such.
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
	
	//When going from one iteration to another in Apriori, this checks if two itemsets differ by exactly one element, and can this be combined to form an itemset of
	//larger size
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
	
	//Combines two itemsets if allowed by the above function. If it can't do so, returns a null
	public static ArrayList<String> tryToCombineItemsets(ArrayList<String> itemSet1, ArrayList<String> itemSet2) {
		
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
	
	//Compares two itemsets if they contain the same items, even if in a different order
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
	
	//Checks if a collection of itemsets already contains an itemset. It makes use of the above function to compare two itemsets
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
	
	//Generates the initial candidate itemsets from the previous iteration's large itemsets
	public static ArrayList<ArrayList<String>> c_KPlusOneFromL_K(ArrayList<ArrayList<String>> itemSets) {
		ArrayList<ArrayList<String>> combinations = new ArrayList<ArrayList<String>>();
		for(int i=0;i<itemSets.size() - 1;i++) {
			for(int j=i+1;j<itemSets.size();j++) {
				ArrayList<String> combo = tryToCombineItemsets(itemSets.get(i), itemSets.get(j)); 
				if(combo != null) {
					if(!containsItemSet(combinations, combo)) {
						combinations.add(combo);
					}
				}
			}
		}
		return combinations;
	}
	
	//Returns all subsets of size (set.size() - 1)
	public static ArrayList<ArrayList<String>> getAllMinusOneSizedSubsets(ArrayList<String> set) {
		int elementToIgnore = 0;
		ArrayList<ArrayList<String>> result = new ArrayList<ArrayList<String>>();
		
		for(int i=0;i<set.size();i++) {
			elementToIgnore = i;
			
			ArrayList<String> subSet = new ArrayList<String>();
			
			for(int j=0;j<set.size();j++) {
				if(j != elementToIgnore) {
					subSet.add(set.get(j));
				}
			}
			
			result.add(subSet);
		}
		return result;
	}
	
	//Helper function to generate all subsets. Our approach uses a boolean mask which increments as a binary number. This functions returns a false when it reaches 
	//a value of (2^n). Else, it 'increments' the mask
	public static boolean incrementMask(boolean mask[]) {
		int lsb = 0;
		
		while(lsb < mask.length && mask[lsb]) {
			mask[lsb] = false;
			lsb += 1;
		}
		
		if(lsb < mask.length) {
			mask[lsb] = true;
			return true;
		} else {
			return false;
		}
	}
	
	//Applies a mask on a set to generate a subset
	public static ArrayList<String> properSubsetFromMask(ArrayList<String> set, boolean[] mask) {
		if(set.size() != mask.length) {
			return null;
		}
		
		ArrayList<String> result = new ArrayList<String>();
		
		int maskCount = 0;
		
		//This makes sure that a mask of all '1' values do not generate a subset equal to the original set
		for(int i=0;i<mask.length;i++) {
			if(mask[i]) {
				maskCount += 1;
			}
		}
		if(maskCount == mask.length) {
			return result;
		}
		
		for(int i=0;i<mask.length;i++) {
			if(mask[i]) {
				result.add(set.get(i));
			}
		}
		
		return result;
	}
	
	//Generates all subsets of the parent itemset
	public static ArrayList<ArrayList<String>> allSubsets(ArrayList<String> parent) {
		ArrayList<ArrayList<String>> result = new ArrayList<ArrayList<String>>();
		
		boolean mask[] = new boolean[parent.size()];
		for(int i=0;i<mask.length;i++) {
			mask[i] = false;
		}
		
		//We increment the mask once before using it, hence avoid adding the empty set
		while(incrementMask(mask)) {
			ArrayList<String> subset = properSubsetFromMask(parent, mask); 
			if(subset != null) {
				if(subset.size() > 0) {
					result.add(subset);
				}
			}
			
		}
		
		return result;
	}
	
	//Finds the complement of a set (value) from a universal set (universe)
	public static ArrayList<String> complement(ArrayList<String> universe, ArrayList<String> value) {
		ArrayList<String> result = new ArrayList<String>();
		for(String element : universe) {
			if(!value.contains(element)) {
				result.add(element);
			}
		}
		return result;
	}
	
	//This is essentially step 2 of the algorithm.
	//Given an itemset, it generates all possible rules and if they satisfy a minimum confidence value, they are accepted and returned
	public static ArrayList<Rule> getRulesFromItemset(ArrayList<String> itemSet, DataSet dataSet, double minConfidence) {
		ArrayList<Rule> rules = new ArrayList<Rule>();
		
		ArrayList<ArrayList<String>> subsets = allSubsets(itemSet);
		
		for(ArrayList<String> subset : subsets) {
			Rule r = new Rule();
			r.LHS = subset;
			r.RHS = complement(itemSet, subset);
			
			ArrayList<String> allItems = new ArrayList<String>();
			allItems.addAll(r.LHS);
			allItems.addAll(r.RHS);
			r.supportFound = dataSet.findSupport(allItems);
			r.confidenceFound = dataSet.findConfidence(r);
			
			if(r.confidenceFound >= minConfidence) {
				rules.add(r);
			}
		}
		
		return rules;
	}
	
	//Core algorithm.
	public static ArrayList<Rule> aprioriMine(DataSet dataSet, double minSupport, double minConfidence) {
		ArrayList<Rule> rules = new ArrayList<Rule>();
		
		//Initially we generate a list of all unique items
		ArrayList<ArrayList<String>> large_k_itemsets = new ArrayList<ArrayList<String>>();
		HashSet<String> uniqueItems = new HashSet<String>();
		for(ArrayList<String> row : dataSet.rows) {
			for(String item : row) {
				if(!uniqueItems.contains(item)) {
					uniqueItems.add(item);
				}
			}
		}
		
		System.out.println("Found the following frequent datasets with min Support as"+minSupport+": ");

		//We filter items by their minimum support
		for(String item : uniqueItems) {
			double supp = dataSet.findSupport(item);
			if(supp >= minSupport) {
				ArrayList<String> initial = new ArrayList<String>();
				initial.add(item);
				large_k_itemsets.add(initial);
				System.out.println("["+item+"] (Supp: "+supp+")");
			}
		}
		

		//This holds the master list of all large itemsets found over all iterations
		ArrayList<ArrayList<String>> largeItemsets = new ArrayList<ArrayList<String>>();
		
		//We add the initial single set items.
		for(ArrayList<String> itemSet : large_k_itemsets) {
			largeItemsets.add(itemSet);
		}
		
		
		while(true) {
			//This list will be populated by the final 'Lk' values
			ArrayList<ArrayList<String>> newItemsets = new ArrayList<ArrayList<String>>();
			
			//We find the initial candidate set by merging all current itemsets to generate itemsets of a larger size
			ArrayList<ArrayList<String>> candidateSets = c_KPlusOneFromL_K(large_k_itemsets);
			
			//This will be used to hold values after pruning those itemsets which contains non-frequent subsets
			ArrayList<ArrayList<String>> candidateSetsPruned = new ArrayList<ArrayList<String>>();
			
			//Prune itemsets such that we only select those whose subsets are all frequently occuring
			for(ArrayList<String> candidateSet : candidateSets) {
				ArrayList<ArrayList<String>> subsets = getAllMinusOneSizedSubsets(candidateSet);
				boolean allSubsetsFrequent = true;
				for(ArrayList<String> subset : subsets) {
					if(!containsItemSet(largeItemsets, subset)) {
						allSubsetsFrequent = false;
						break;
					}	
				}
				if(allSubsetsFrequent) {
					candidateSetsPruned.add(candidateSet);
				}
			}
			
			//Choose only candidate sets who satisfy minimum support
			for(ArrayList<String> itemSet : candidateSetsPruned) {
				double supp = dataSet.findSupport(itemSet);
				if(supp >= minSupport) {
					System.out.println(itemSet+" (Supp: "+supp+")");
					newItemsets.add(itemSet);
				}
			}
			
			
			if(newItemsets.size() == 0) {
				//We have found all large itemsets
				break;
			} else {
				for(ArrayList<String> is : newItemsets) {
					largeItemsets.add(is);
				}
			}
			
			//And we iterate again...
			large_k_itemsets = newItemsets;
		}
		
		
		//System.out.println((largeItemsets));
		
		//Step 2 of our algorithm. We call the function to generate rules on all of our large itemsets. 
		for(ArrayList<String> itemSet : largeItemsets) {
			rules.addAll(getRulesFromItemset(itemSet, dataSet, minConfidence));
		}
		
		return rules;
	}
	
	public static void main(String args[]) {
		String[] myArg = {"D:\\EclipseWorkspace\\AprioriMiner\\INTEGRATED-DATASET.csv", "0.1", "1.0"};
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
		
		System.out.println(myDataSet.rows);
		
		System.out.println("\nMining frequent itemsets and rules...");
		ArrayList<Rule> rulesFound = aprioriMine(myDataSet, minSupport, minConfidence);
		
		Collections.sort(rulesFound);
		
		System.out.println("\nFound the following "+rulesFound.size()+" rules with min Confidence as "+minSupport+": ");
		for(Rule r : rulesFound) {
			System.out.println(r.printToString());
		}
	}

}
