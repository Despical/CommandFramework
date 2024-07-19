package me.despical.commandframework.options;

import me.despical.commandframework.CommandArguments;

/**
 * @author Despical
 * <p>
 * Created at 18.07.2024
 *
 * @see Option#CUSTOM_COOLDOWN_CHECKER
 * @see Option#CONFIRMATIONS
 */
public enum Option {

	/**
	 * This option allows user to call {@link CommandArguments#checkCooldown()} method.
	 */
	CUSTOM_COOLDOWN_CHECKER,

	/**
	 * This option allows users to check for command confirmations.
	 */
	CONFIRMATIONS
}