package com.hljunlp.laozhongyi.strategy.checkpoint;

import com.hljunlp.laozhongyi.checkpoint.SimulatedAnnealingCheckPointData;
import com.hljunlp.laozhongyi.checkpoint.SimulatedAnnealingCheckPointManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class SimulatedAnnealingCheckPointManagerTest {

    @Test
    void saveAndLoadCheckPoint() {
        // Create a SimulatedAnnealingCheckPointData object with some values
        Map<String, String> currentHyperParameters = new HashMap<>();
        currentHyperParameters.put("param1", "value1");
        currentHyperParameters.put("param2", "value2");
        Map<String, String> bestHyperParameters = new HashMap<>();
        bestHyperParameters.put("param3", "value3");
        bestHyperParameters.put("param4", "value4");
        SimulatedAnnealingCheckPointData data = new SimulatedAnnealingCheckPointData(1.0f, 0.5f,
                currentHyperParameters, 2, bestHyperParameters, 0.9f);

        // Save the SimulatedAnnealingCheckPointData object to a file
        String filePath = "checkpoint.json";
        SimulatedAnnealingCheckPointManager manager =
                new SimulatedAnnealingCheckPointManager(filePath);
        manager.save(data);

        // Load the SimulatedAnnealingCheckPointData object from the file
        SimulatedAnnealingCheckPointData loadedData =
                (SimulatedAnnealingCheckPointData) manager.load();

        // Compare the loaded SimulatedAnnealingCheckPointData object with the original one
        Assertions.assertEquals(data.getTemperature(), loadedData.getTemperature());
        Assertions.assertEquals(data.getDecayRate(), loadedData.getDecayRate());
        Assertions.assertEquals(data.getCurrentHyperParametersIndex(),
                loadedData.getCurrentHyperParametersIndex());
        Assertions.assertEquals(data.getBestScore(), loadedData.getBestScore());
        Assertions.assertEquals(data.getCurrentHyperParameters().size(),
                loadedData.getCurrentHyperParameters().size());
        Assertions.assertEquals(data.getBestHyperParameters().size(),
                loadedData.getBestHyperParameters().size());
    }

    @Test
    void loadNonExistentCheckPoint() {
        String filePath = "checkpoint.json";
        SimulatedAnnealingCheckPointManager manager =
                new SimulatedAnnealingCheckPointManager(filePath);
        Assertions.assertThrows(RuntimeException.class, manager::load);
    }
}
