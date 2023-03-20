package com.hljunlp.laozhongyi.checkpoint;

import org.json.JSONObject;

public class SimulatedAnnealingCheckPointManager extends CheckPointManager {
    /**
     * Constructor.
     *
     * @param checkPointPath The path of the checkpoint file.
     */
    public SimulatedAnnealingCheckPointManager(final String checkPointPath) {
        super(checkPointPath);
    }

    @Override
    protected JSONObject convertToJSON(final CheckPointData data) {
        JSONObject jsonObject = super.convertToJSON(data);
        SimulatedAnnealingCheckPointData simulatedAnnealingCheckPointData =
                (SimulatedAnnealingCheckPointData) data;
        jsonObject.put("temperature", simulatedAnnealingCheckPointData.getTemperature());
        jsonObject.put("decayRate", simulatedAnnealingCheckPointData.getDecayRate());
        return jsonObject;
    }

    @Override
    protected CheckPointData convertFromJSON(final JSONObject jsonObject) {
        CheckPointData data = super.convertFromJSON(jsonObject);
        float temperature = (float) jsonObject.getDouble("temperature");
        float decayRate = (float) jsonObject.getDouble("decayRate");
        return new SimulatedAnnealingCheckPointData(temperature, decayRate,
                data.getCurrentHyperParameters(), data.getCurrentHyperParametersIndex(),
                data.getBestHyperParameters(), data.getBestScore());
    }
}
