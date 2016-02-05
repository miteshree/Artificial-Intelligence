import java.io.*;
import java.util.*;

public class CheckTrueFalse {

	static Set<String> symbolList = new HashSet<String>();

	public static void main(String[] args) {

		if (args.length != 3) {
			System.out.println("Usage: "+ args[0]+ " [wumpus-rules-file] [additional-knowledge-file] [input_file]\n");
			exit_function(0);
		}
		String buffer;
		BufferedReader inputStream;
		LogicalExpression kb_plus_rules = new LogicalExpression();

		LogicalExpression statement1 = new LogicalExpression();
		LogicalExpression statement2 = new LogicalExpression();
		TTEntailsAlgorithm ttea = new TTEntailsAlgorithm();
		TTEntailsAlgorithm.Model m = ttea.new Model();
		try {
			inputStream = new BufferedReader(new FileReader(args[0]));
			kb_plus_rules.setConnective("and");

			while ((buffer = inputStream.readLine()) != null) {
				if (!(buffer.startsWith("#") || (buffer.equals("")))) {
					LogicalExpression subExpression = readExpression(buffer);
					kb_plus_rules.setSubexpression(subExpression);

				}
			}
			inputStream.close();

		} catch (Exception e) {
			System.out.println("failed to open " + args[0]);
			e.printStackTrace();
			exit_function(0);
		}

		try {
			inputStream = new BufferedReader(new FileReader(args[1]));			
			while ((buffer = inputStream.readLine()) != null) {
				if (!(buffer.startsWith("#") || (buffer.equals("")))) {

					String temp = buffer;
					if (temp.contains("not")) {
						String split[] = temp.split(" ");
						split[1] = split[1].substring(0, split[1].length() - 1);
						m.h.put(split[1], false);
					} else {
						temp = temp.trim();
						m.h.put(temp, true);
					}
					LogicalExpression subExpression = readExpression(buffer);
					kb_plus_rules.setSubexpression(subExpression);
				}
			}
			inputStream.close();

		} catch (Exception e) {
			System.out.println("failed to open " + args[1]);
			e.printStackTrace();
			exit_function(0);
		}
		if (!valid_expression(kb_plus_rules)) {
			System.out.println("invalid knowledge base");
			exit_function(0);
		}
		String alpha1 = "";
		String alpha2 = "";
		try {
			inputStream = new BufferedReader(new FileReader(args[2]));
			getSymbols(kb_plus_rules);
			Set<String> uniqueSymbolset = symbolList;
			while ((buffer = inputStream.readLine()) != null) {
				if (!buffer.startsWith("#")) {
					if (buffer.contains("not")) {
						alpha1 = buffer;
						String split[] = buffer.split(" ");
						alpha2 = split[1].substring(split[1].length() - 1);
					} else {
						alpha1 = buffer;
						alpha2 = "(not " + buffer + ")";
					}

					statement1 = readExpression(alpha1);
					statement2 = readExpression(alpha2);
					if (valid_expression(statement1)&& !isValidInput(alpha1, uniqueSymbolset)) {
						System.out.println("invalid statement");
						return;
					}
					break;
				} 
			}
			inputStream.close();

		} catch (Exception e) {
			System.out.println("failed to open " + args[2]);
			e.printStackTrace();
			exit_function(0);
		}
		if (!valid_expression(statement1)) {
			System.out.println("invalid statement");
			exit_function(0);
		}

		
		boolean output1 = ttea.ttEntails(kb_plus_rules, m, statement1);
		boolean output2 = ttea.ttEntails(kb_plus_rules, m, statement2);

		printOutput(output1, output2);

	}

	private static void printOutput(boolean output1, boolean output2) {
		try {
			BufferedWriter writer;
			writer = new BufferedWriter(new FileWriter(new File("result.txt")));

			if (output1 != output2) {
				System.out.println("definitely " + output1);
				writer.write("definitely " + output1);
			} else if (output1 == output2 && output1 == false) {
				System.out.println("possibly true, possibly false");
				writer.write("possibly true, possibly false");
			} else if (output1 == output2 && output1 == true) {
				System.out.println("both true and false");
				writer.write("both true and false");
			}
			writer.close();

		} catch (IOException e) {
			System.out.println("Error message : " + e.getMessage());
			e.printStackTrace();
		}

	}

	public static LogicalExpression readExpression(String input_string) {
		LogicalExpression result = new LogicalExpression();
		input_string = input_string.trim();

		if (input_string.startsWith("(")) {

			String symbolString = "";
			symbolString = input_string.substring(1);
			if (!symbolString.endsWith(")")) {
				System.out.println("missing ')' !!! - invalid expression! - readExpression():-"	+ symbolString);
				exit_function(0);

			} else {
				symbolString = symbolString.substring(0,
						(symbolString.length() - 1));
				symbolString.trim();
				symbolString = result.setConnective(symbolString);
			}

			result.setSubexpressions(read_subexpressions(symbolString));

		} else {
			result.setUniqueSymbol(input_string);
		}

		return result;
	}

	public static Vector<LogicalExpression> read_subexpressions(String input_string) {

		Vector<LogicalExpression> symbolList = new Vector<LogicalExpression>();
		LogicalExpression newExpression;
		String newSymbol = new String();
		input_string.trim();
		while (input_string.length() > 0) {

			newExpression = new LogicalExpression();

			if (input_string.startsWith("(")) {
								int parenCounter = 1;
				int matchingIndex = 1;
				while ((parenCounter > 0)&& (matchingIndex < input_string.length())) {
					if (input_string.charAt(matchingIndex) == '(') {
						parenCounter++;
					} else if (input_string.charAt(matchingIndex) == ')') {
						parenCounter--;
					}
					matchingIndex++;
				}

				newSymbol = input_string.substring(0, matchingIndex);

				newExpression = readExpression(newSymbol);

				symbolList.add(newExpression);

				input_string = input_string.substring(newSymbol.length(),input_string.length());

			} else {
				if (input_string.contains(" ")) {
					newSymbol = input_string.substring(0,input_string.indexOf(" "));
					input_string = input_string.substring((newSymbol.length() + 1), input_string.length());
				} else {
					newSymbol = input_string;
					input_string = "";
				}


				newExpression.setUniqueSymbol(newSymbol);

				symbolList.add(newExpression);

			}
			input_string.trim();

			if (input_string.startsWith(" ")) {
				input_string = input_string.substring(1);
			}
		}
		return symbolList;
	}

	public static boolean valid_expression(LogicalExpression expression) {
		if (!(expression.getUniqueSymbol() == null)
				&& (expression.getConnective() == null)) {
			return valid_symbol(expression.getUniqueSymbol());
		}

		if ((expression.getConnective().equalsIgnoreCase("if"))	|| (expression.getConnective().equalsIgnoreCase("iff"))) {
			if (expression.getSubexpressions().size() != 2) {
				System.out.println("error: connective \""	+ expression.getConnective() + "\" with "	+ expression.getSubexpressions().size()	+ " arguments\n");
				return false;
			}
		}
		else if (expression.getConnective().equalsIgnoreCase("not")) {
			if (expression.getSubexpressions().size() != 1) {
				System.out.println("error: connective \""+ expression.getConnective() + "\" with "+ expression.getSubexpressions().size()+ " arguments\n");
				return false;
			}
		}
		else if ((!expression.getConnective().equalsIgnoreCase("and"))
				&& (!expression.getConnective().equalsIgnoreCase("or"))
				&& (!expression.getConnective().equalsIgnoreCase("xor"))) {
			System.out.println("error: unknown connective "	+ expression.getConnective() + "\n");
			return false;
		}
		
		for (
		Enumeration<LogicalExpression> e = expression.getSubexpressions().elements(); e.hasMoreElements();) {
			LogicalExpression testExpression = (LogicalExpression) e.nextElement();
			if (!valid_expression(testExpression)) {
				return false;
			}
		}
		return true;
	}

	public static boolean valid_symbol(String symbol) {
		if (symbol == null || (symbol.length() == 0)) {
			return false;
		}

		for (int counter = 0; counter < symbol.length(); counter++) {
			if ((symbol.charAt(counter) != '_')
					&& (!Character.isLetterOrDigit(symbol.charAt(counter)))) {

				System.out.println("String: " + symbol	+ " is invalid! Offending character:---"+ symbol.charAt(counter) + "---\n");

				return false;
			}
		}

		return true;
	}

	private static void exit_function(int value) {
		System.out.println("exiting from checkTrueFalse");
		System.exit(value);
	}

	static boolean isValidInput(String s, Set<String> set) {

		Iterator<String> it = set.iterator();
		boolean b = false;
		while (it.hasNext()) {
			if (it.next().equals(s))
				b = true;
		}
		if (s.contains("(or") || s.contains("(and") || s.contains("(xor")	|| s.contains("(not") || s.contains("(if")	|| s.contains("(iff"))
			b = true;
		return b;
	}

	static void getSymbols(LogicalExpression le) {
		if (le.getUniqueSymbol() != null)
			symbolList.add(le.getUniqueSymbol());
		else
			for (int i = 0; i < le.getSubexpressions().size(); i++) {
				LogicalExpression lle = (LogicalExpression) le.getSubexpressions().get(i);
				getSymbols(lle);
				if (lle.getUniqueSymbol() != null)
					symbolList.add(lle.getUniqueSymbol());				
			}

	}
}
