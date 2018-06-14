/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.cypherpunk.statelearner;

import java.util.LinkedList;
import java.util.List;
import net.automatalib.automata.transout.MealyMachine;
import net.automatalib.words.impl.SimpleAlphabet;


public class HandshakeAnalyzer {
    
    private static final String FINISHED_MESSAGE = "FINISHED";
    
    public static boolean containsValidHandshake(MealyMachine<?, String, ?, String> model, SimpleAlphabet<String> alphabet) {
        MealyMachine<Object, String, ?, String> tmpModel = (MealyMachine<Object, String, ?, String>) model;
        
        
        for (Object state : tmpModel.getStates()) {
            for(String input : alphabet) {
                String output = tmpModel.getOutput(state, input);
                if(output.contains(FINISHED_MESSAGE)) {
                    return true;
                }
            }
        }
        return false;
    }
}
