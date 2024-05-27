package com.liveinside.DocuFlow.generators;

import java.util.Random;

public class GeneratorUniqNumber {
    Random random = new Random();

    public int generateDocNumber() {
        return random.nextInt(Integer.MAX_VALUE);
    }

}
