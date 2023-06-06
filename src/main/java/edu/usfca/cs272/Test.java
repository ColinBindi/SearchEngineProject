package edu.usfca.cs272;

import java.util.Arrays;

/**
 * Class that tests code.
 * 
 * @author colinbindi
 *
 */
public class Test {
	/**
	 * Starts a Jetty server on port 8080.
	 *
	 * @param args unused
	 */
	public static void main(String[] args) {
		//String str = "-html https://usf-cs272-spring2022.github.io/project-web/input/simple/ -max 50 -limit 15 -threads 3 -server 8080";
		
		//String str = "-html https://usf-cs272-spring2022.github.io/project-web/input/guten/ -max 50 -limit 15 -threads 3 -server 8080";
		
		String str = "-html https://www.cs.usfca.edu/~cs212/javadoc/api/allclasses-index.html -max 50 -server 8080 -threads 5";
		
		String[] arguments = str.split(" ");
		
		System.out.println("Args: "+Arrays.toString(arguments));
		
		Driver.main(arguments);
	}
}
