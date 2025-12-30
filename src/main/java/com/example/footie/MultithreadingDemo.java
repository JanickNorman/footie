package com.example.footie;


public class MultithreadingDemo {

	public static void main(String[] args) {
		for (int i = 1; i <= 100; i++) {

			StringBuffer s = new StringBuffer();
			if (i % 3 == 0 & i % 5 == 0) {
				s.append("FizzBuzz");
			} else if (i % 3 == 0) {
				s.append("Fizz");
			} else if (i % 5 == 0) {
				s.append("Buzz");
			} else {
				s.append(i);
			}

			System.out.println(s.toString());
		}
	}

}
