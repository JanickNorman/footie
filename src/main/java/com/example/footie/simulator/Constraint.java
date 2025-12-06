/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.example.footie.simulator;

import java.util.Map;
import java.util.Set;

/**
 * Constraint for the World Cup draw CSP.
 * Implementations should define whether assigning a candidate {@link Team}
 * to a given slot is consistent with the constraint, and whether the current
 * assignments satisfy the constraint.
 */
public interface Constraint {

	/**
	 * Check whether assigning {@code team} to {@code slot} would be consistent
	 * with this constraint given the current partial {@code assignments} and
	 * variable {@code domains}.
	 *
	 * @param slot the variable/slot (e.g. "A1")
	 * @param team the candidate team to assign
	 * @param assignments current partial assignments (slot -> team)
	 * @param domains current domains for each slot
	 * @return true if the assignment is consistent with the constraint
	 */
	boolean isConsistent(String slot, Team team, Map<String, Team> assignments, Map<String, Set<Team>> domains);

	/**
	 * Check whether the provided (partial or complete) assignments satisfy
	 * this constraint.
	 *
	 * @param assignments current partial/complete assignments
	 * @return true if the assignments satisfy the constraint
	 */
	boolean isSatisfied(Map<String, Team> assignments);
}
