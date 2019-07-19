import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Scanner;

import net.datastructures.LinkedStack;
import net.datastructures.Stack;


public class Main {

	public static Stack<String> studentInfo;
	public static Stack<String> studentTimes;
	private static Scanner scanner;

	public static void main(String[] args) throws IOException {

		// code for file number input
		scanner = new Scanner( System.in );
		System.out.print("Test cases 8-10 were originally made. Please input a test data number (1-10): ");
		int testDataNum = Integer.parseInt(scanner.nextLine());   

		String fileTitle = "";

		while (testDataNum > 10 || testDataNum < 1) { // keep asking for another input until the input is valid (1-10)

			System.out.print("That was an invalid file number. Please try again: ");
			testDataNum = Integer.parseInt(scanner.nextLine());   

		}

		// building the file title with the inputted number and getting the text from the file
		fileTitle = "proj2_set" + testDataNum + ".txt";
		String input = new String(Files.readAllBytes(Paths.get(fileTitle)));

		// some variables
		studentInfo = new LinkedStack<String>();
		studentTimes = new LinkedStack<String>();
		
		String[] dayIndex = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};
		String[] dayIndexAbbr = {"M", "T", "W", "H", "F"};
		String[] timeIndex = {"0900", "1000", "1100", "1200", "1300", "1400", "1500", "1600", "1700", "1800", "1900", "2000", "2100"};
		
		Integer[][] tutorScheduling = new Integer[dayIndex.length][timeIndex.length];
		for (int i = 0; i < tutorScheduling.length; i++) { // need to initialize 2D array w/ zeros
			Arrays.fill(tutorScheduling[i], 0);
		}

		// put students into studentInfo after separating them by parentheses
		studentInfo = divideStudents(input);

		while (!studentInfo.isEmpty()) { // for each student in the stack

			String currentStudent = studentInfo.pop();

			currentStudent = currentStudent.substring(currentStudent.indexOf("<")); // gets rid of name

			if (specialIsMatched(currentStudent)) {
				// the data is all good, proceed to analysis
				// the call of specialIsMatched in the if condition puts the day/times in studentTimes

				while (!studentTimes.isEmpty()) { // for each day/time in studentTimes
					
					String currentTime = studentTimes.pop(); // each string will be formatted like M0900 for example

					String day = currentTime.substring(0, 1); // get letter of day
					String time = currentTime.substring(1); // get time code

					// index variables to know where to store the day/time in the 2D array (starts as -1 just in case a time given is not in the range 0900-2100)
					int dayInt = -1;
					int timeInt = -1;

					for (int i = 0; i < dayIndexAbbr.length; i++) {
						if (day.equals(dayIndexAbbr[i])) {
							dayInt = i;
						}
					}
					for (int i = 0; i < timeIndex.length; i++) {
						if (time.equals(timeIndex[i])) {
							timeInt = i;
						}
					}
					if (dayInt != -1 && timeInt != -1) { // make sure the time was in the range, if it wasn't, nothing happens with the time
						tutorScheduling[dayInt][timeInt]++;
					}

				}

			} else { // the data was bad, need to empty studentTimes for the next student
				while (!studentTimes.isEmpty()) {
					studentTimes.pop();
				}
			}


		}

		// 2d array is filled, now printing & finding best time slot
		int highest = 0;
		int highestRow = 0;
		int highestColumn = 0;
		
		System.out.println();
		System.out.println("          | 09:00 | 10:00 | 11:00 | 12:00 | 13:00 | 14:00 | 15:00 | 16:00 | 17:00 | 18:00 | 19:00 | 20:00 | 21:00 |");
		
		for (int i = 0; i < tutorScheduling.length; i++) { // for each row in the tutorScheduling array
			printDashes();
			String toPrint = dayIndex[i];
			while (toPrint.length() != 9) { // makes sure all the titles of the rows are the same length
				toPrint += " ";
			}
			System.out.print(toPrint + " |");
			for (int k = 0; k < tutorScheduling[i].length; k++) { // for each String in the array in tutorScheduling[i]
				System.out.print("   " + tutorScheduling[i][k] + "   |");
				if (tutorScheduling[i][k] > highest) {
					highest = tutorScheduling[i][k];
					highestRow = i;
					highestColumn = k;
				}
			}
			System.out.println();
		}
		printDashes();
		System.out.println();
		System.out.println("The best slot is on " + dayIndex[highestRow] + " at " + timeIndex[highestColumn] + ".");

		// figuring out what students are in that time slot
		Stack<String> students = divideStudents(input);
		Stack<String> studentsOnBestTime = findNamesWithTime(students, dayIndexAbbr[highestRow], timeIndex[highestColumn]);
		System.out.print("The following students are available for that time slot: ");
		int origSize = studentsOnBestTime.size();
		while (!studentsOnBestTime.isEmpty()) {
			System.out.print(studentsOnBestTime.pop());
			if (studentsOnBestTime.size() != 0) {
				if (origSize > 2) {
					System.out.print(", ");
				} else {
					System.out.print(" ");
				}
			}
			if(studentsOnBestTime.size() == 1) {
				System.out.print("and ");
			}
		}
		System.out.print(".");


	}

	public static Stack<String> divideStudents(String input) { // method divides students by parentheses and returns stack with each string
		Stack<String> toReturn = new LinkedStack<>();
		int j = input.indexOf('('); // find first ’(’ character (if any)
		while (j != -1) {
			int k = input.indexOf(')', j+1); // find next ’)’ character
			if (k == -1 || ((k > input.indexOf('(', j+1) && input.indexOf('(',j+1) != -1))) {
				j = input.indexOf('(', j+1); // CHANGED TO J+1 FROM K+1
				continue; // invalid tag --> have to continue
			}

			String tag = input.substring(j+1, k); // strip away ( )
			toReturn.push(tag);

			j = input.indexOf('(', k+1); // find next ’(’ character (if any)
		}
		return toReturn;
	}

	public static boolean specialIsMatched(String expression) {  // a modified isMatched method that is only called once

		final String opening = "<["; // opening delimiters
		final String closing = ">]"; // respective closing delimiters

		boolean foundLeftCarrot = false;
		boolean foundLeftBracket = false;
		int counter = 0;
		String currentDay = "";
		String currentTime = "";

		Stack<Character> buffer = new LinkedStack<>( );

		for (char c : expression.toCharArray( )) {  

			if (foundLeftCarrot) {
				currentDay += c;
				foundLeftCarrot = false;
			}
			if (foundLeftBracket) {
				if (counter <= 3) {
					currentTime += c;
					counter++;
				} else {
					foundLeftBracket = false;
					studentTimes.push(currentDay + currentTime);
					currentTime = "";
					counter = 0;
				}
			}

			if (opening.indexOf(c) != -1) {// this is a left delimiter

				buffer.push(c);
				if (c == '<') {
					foundLeftCarrot = true;
				}
				if (c == '[') {
					foundLeftBracket = true;
				}

			} else if (closing.indexOf(c) != -1) { // this is a right delimiter

				if (buffer.isEmpty( )) { // nothing to match with
					return false;
				}

				if (closing.indexOf(c) != opening.indexOf(buffer.pop( ))) {
					return false; // mismatched delimiter
				}

				// after 2 if statements, this is the correct right delimiter
				if (c == '>') {
					currentDay = "";
				}

			}

		}

		return buffer.isEmpty( ); // were all opening delimiters matched?

	}

	public static Stack<String> findNamesWithTime(Stack<String> input, String day, String time) {
		// goes through the String from the file and returns a stack with names that correspond to the day/time parameters
		// does not include ill-formed records
		
		Stack<String> toReturn = new LinkedStack<>();

		while (!input.isEmpty()) {
			String currentStudent = input.pop();
			if (regularIsMatched(currentStudent)) {
				if (currentStudent.indexOf(day) >= 0) {
					String temp = currentStudent.substring(currentStudent.indexOf(day));
					if (temp.indexOf(time) != -1 && (temp.indexOf(time) < temp.indexOf(">")))

						toReturn.push(currentStudent.substring(0, currentStudent.indexOf("<")));
				}
			}
		}

		return toReturn;
	}

	public static boolean regularIsMatched(String expression) {  // a barely modified isMatched method; called in findNamesWithTime()
		final String opening = "(<["; // opening delimiters
		final String closing = ")>]"; // respective closing delimiters
		Stack<Character> buffer = new LinkedStack<>( );
		for (char c : expression.toCharArray( )) {  
			if (opening.indexOf(c) != -1) // this is a left delimiter
				buffer.push(c);
			else if (closing.indexOf(c) != -1) { // this is a right delimiter
				if (buffer.isEmpty( )) // nothing to match with
					return false;
				if (closing.indexOf(c) != opening.indexOf(buffer.pop( )))
					return false; // mismatched delimiter
			}
		}
		return buffer.isEmpty( ); // were all opening delimiters matched?
	}

	public static void printDashes() { // prints the correct number of dashes for the table
		for (int l = 0; l < 115; l++) {
			System.out.print("-");
		}
		System.out.println();
	}


}


