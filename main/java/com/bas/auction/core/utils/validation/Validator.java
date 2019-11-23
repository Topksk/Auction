package com.bas.auction.core.utils.validation;

import org.apache.commons.validator.routines.EmailValidator;
import org.apache.commons.validator.routines.RegexValidator;
import org.apache.commons.validator.routines.checkdigit.IBANCheckDigit;

import com.bas.auction.core.utils.validation.PasswordValidator.Strength;

public class Validator {
	private static final IBANCheckDigit ibanValidator = new IBANCheckDigit();
	private static final IinBinValidator nnValidator = new IinBinValidator();
	private static final RnnValidator rnnValidator = new RnnValidator();
	private static final EmailValidator emailValidator = EmailValidator.getInstance();
	private static final PasswordValidator passwordValidator = new PasswordValidator();
	private static final RegexValidator phoneValidator = new RegexValidator(
			"^\\+?\\s*\\d\\s*\\(\\d{3}\\)\\s*\\d{3}-?\\d{2}-?\\d{2}$");

	/**
	 * Validates IIN or BIN value
	 * 
	 * @param nn
	 *            IIN or BIN
	 * @return
	 */
	public static boolean isValidIinOrBin(String nn) {
		return nnValidator.isValid(nn);
	}

	/**
	 * Validates RNN value
	 * 
	 * @param rnn
	 * @return
	 */
	public static boolean isValidRnn(String rnn) {
		return rnnValidator.isValid(rnn);
	}

	public static Strength isValidPassword(String password) {
		return passwordValidator.isValid(password);
	}

	/**
	 * Validates IBAN value. This method internally uses apache commons
	 * validator library implementation.
	 * 
	 * @param iban
	 *            IBAN
	 * @return
	 */
	public static boolean isValidIBAN(String iban) {
		return ibanValidator.isValid(iban);
	}

	/**
	 * Validates email address. This method internally uses apache commons
	 * validator library implementation.
	 * 
	 * @param email
	 *            E-mail address
	 * @return
	 */
	public static boolean isValidEmail(String email) {
		return emailValidator.isValid(email);
	}

	/**
	 * Validates phone number. Method uses {@code ^\+\s*\d\s*\(\d 3}
	 * \)\s*\d{3}-\d{2}-\d{2}$} regular expression for validation. Example of
	 * valid phone number is +7 (777) 777-77-77.
	 * 
	 * @param number
	 *            Phone number
	 * @return
	 */
	public static boolean isValidPhoneNumber(String number) {
		return phoneValidator.isValid(number);
	}
}
