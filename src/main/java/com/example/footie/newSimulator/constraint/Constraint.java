/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */

package com.example.footie.newSimulator.constraint;

import com.example.footie.newSimulator.AssignmentState;
import com.example.footie.newSimulator.GroupSlot;
import com.example.footie.newSimulator.Team;

public interface Constraint {

    public boolean isAssignmentAllowed(AssignmentState state, GroupSlot slot, Team team);

    public void forwardCheck(AssignmentState state, GroupSlot slot, Team team);

}
