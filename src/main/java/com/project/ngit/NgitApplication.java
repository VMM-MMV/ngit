package com.project.ngit;

import java.util.Scanner;

public class NgitApplication {

	static boolean isRunning = true;
	public static void main(String[] args) {
		try (Scanner scanner = new Scanner(System.in);) {

			while (isRunning) {
				String input = scanner.nextLine();
				System.out.println(input);
			}

		} catch (Exception e) {
			System.out.println(e);
		}
	}

}
