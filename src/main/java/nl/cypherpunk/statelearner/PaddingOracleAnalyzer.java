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

/**
 * Analyzes transitions and states to automatically detect whether the server
 * is vulnerable to padding oracle attacks. Note that the words in the 
 * alphabet have to start with Pad or Bleichenbacher
 */
public class PaddingOracleAnalyzer {
    
    private static final String PADDING_MESSAGE_START = "Pad";
    
    private static final String BLEICHENBACHER_MESSAGE_START = "bleichenbacher";

    public static boolean isVulnerableToCbcPaddingOracleAttack(MealyMachine<?, String, ?, String> model, SimpleAlphabet<String> alphabet) {
        return isVulnerableToPaddingOracleAttack(model, alphabet, PADDING_MESSAGE_START);
    }
    
    public static boolean isVulnerableToBleichenbacherAttack(MealyMachine<?, String, ?, String> model, SimpleAlphabet<String> alphabet) {
        return isVulnerableToPaddingOracleAttack(model, alphabet, BLEICHENBACHER_MESSAGE_START);
    }
    
    private static boolean isVulnerableToPaddingOracleAttack(MealyMachine<?, String, ?, String> model, SimpleAlphabet<String> alphabet, String transitionFilter) {
        MealyMachine<Object, String, ?, String> tmpModel = (MealyMachine<Object, String, ?, String>) model;
        List<String> analyzedTransitions = new LinkedList<>();
        for (String s : alphabet) {
            if (s.startsWith(transitionFilter)) {
                analyzedTransitions.add(s);
            }
        }
        System.out.println("Transitions to analyze: " + analyzedTransitions);

        boolean vulnerable = false;

        for (Object s : tmpModel.getStates()) {
            Object refDst = tmpModel.getSuccessor(s, analyzedTransitions.get(0));
            String refOut = tmpModel.getOutput(s, analyzedTransitions.get(0));

            for (int i = 1; i < analyzedTransitions.size(); i++) {
//                if (refOut.equals("not interested")) {
//                    continue;
//                }
                // check whether the output (response messages) as well as 
                // the resulting states are the same
                boolean ok = (refOut.equals(tmpModel.getOutput(s, analyzedTransitions.get(i)))
                        && refDst.equals(tmpModel.getSuccessor(s, analyzedTransitions.get(i))));
                if (!ok) {
                    vulnerable = true;
//                    System.out.println("ref dest1: " + refDst);
//                    System.out.println("ref dest2: " + tmpModel.getSuccessor(s, analyzedTransitions.get(i)));
//                    System.out.println("ref out1:  " + refOut);
//                    System.out.println("ref out2:  " + tmpModel.getOutput(s, analyzedTransitions.get(i)));
                }
            }
        }
        return vulnerable;
    }
}
