package com.pedrohenrique.pagcontrolback.utils;

public class ValidateCnpj {

    public static boolean isValidCnpj(String cnpj) {
        if (cnpj == null || cnpj.length() != 14 || !cnpj.matches("\\d{14}")) {
            return false;
        }

        int[] weights1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int[] weights2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

        String cnpjWithoutDigits = cnpj.substring(0, 12);
        String digits = cnpj.substring(12);

        int sum1 = calculateWeightedSum(cnpjWithoutDigits, weights1);
        int digit1 = calculateDigit(sum1);

        int sum2 = calculateWeightedSum(cnpjWithoutDigits + digit1, weights2);
        int digit2 = calculateDigit(sum2);

        return digits.equals(String.valueOf(digit1) + digit2);
    }

    private static int calculateWeightedSum(String cnpjPart, int[] weights) {
        int sum = 0;
        for (int i = 0; i < cnpjPart.length(); i++) {
            sum += Character.getNumericValue(cnpjPart.charAt(i)) * weights[i];
        }
        return sum;
    }

    private static int calculateDigit(int sum) {
        int remainder = sum % 11;
        return (remainder < 2) ? 0 : (11 - remainder);
    }
}
