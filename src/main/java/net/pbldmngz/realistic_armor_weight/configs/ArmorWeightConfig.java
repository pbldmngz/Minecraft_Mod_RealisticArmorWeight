package net.pbldmngz.realistic_armor_weight.configs;

import com.google.gson.*;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class ArmorWeightConfig {
    private static final String CONFIG_FILE_NAME = "armorweight.json";
    private final Path configPath;

    private Map<String, Float> armorWeights;
    private boolean enableCounterAttributes;
    private boolean enableSpeedJump;
    private float elytraSpeedBonus;
    private float elytraFallResistanceBonus;
    private float speedAttackMultiplier;

    public ArmorWeightConfig() {
        this.configPath = FabricLoader.getInstance().getConfigDir().resolve(CONFIG_FILE_NAME);
        this.armorWeights = new HashMap<>();
    }

    public void loadConfig() {
        if (configPath.toFile().exists()) {
            try (Reader reader = new FileReader(configPath.toFile())) {
                JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();

                if (jsonObject.has("armorWeights")) {
                    JsonObject armorWeightsJson = jsonObject.getAsJsonObject("armorWeights");
                    for (Map.Entry<String, JsonElement> entry : armorWeightsJson.entrySet()) {
                        armorWeights.put(entry.getKey(), entry.getValue().getAsFloat());
                    }
                }

                enableCounterAttributes = jsonObject.has("enableCounterAttributes") ? jsonObject.get("enableCounterAttributes").getAsBoolean() : true;
                enableSpeedJump = jsonObject.has("enableSpeedJump") ? jsonObject.get("enableSpeedJump").getAsBoolean() : true;
                elytraSpeedBonus = jsonObject.has("elytraSpeedBonus") ? jsonObject.get("elytraSpeedBonus").getAsFloat() : 0.2f;
                elytraFallResistanceBonus = jsonObject.has("elytraFallResistanceBonus") ? jsonObject.get("elytraFallResistanceBonus").getAsFloat() : 0.5f;
                speedAttackMultiplier = jsonObject.has("speedAttackMultiplier") ? jsonObject.get("speedAttackMultiplier").getAsFloat() : 0.1f;
            } catch (IOException | JsonParseException e) {
                e.printStackTrace();
                createDefaultConfig();
            }
        } else {
            createDefaultConfig();
        }
    }

    private void createDefaultConfig() {
        armorWeights.put("leather", 0.015f);
        armorWeights.put("chainmail", 0.025f);
        armorWeights.put("iron", 0.06f);
        armorWeights.put("gold", 0.035f);
        armorWeights.put("diamond", 0.075f);
        armorWeights.put("netherite", 0.09f);

        enableCounterAttributes = true;
        enableSpeedJump = true;
        elytraSpeedBonus = 0.3f;
        elytraFallResistanceBonus = 2.5f;
        speedAttackMultiplier = 1f;

        saveConfig();
    }

    public void saveConfig() {
        JsonObject jsonObject = new JsonObject();

        JsonObject armorWeightsJson = new JsonObject();
        for (Map.Entry<String, Float> entry : armorWeights.entrySet()) {
            armorWeightsJson.addProperty(entry.getKey(), entry.getValue());
        }
        jsonObject.add("armorWeights", armorWeightsJson);

        jsonObject.addProperty("enableCounterAttributes", enableCounterAttributes);
        jsonObject.addProperty("enableSpeedJump", enableSpeedJump);
        jsonObject.addProperty("elytraSpeedBonus", elytraSpeedBonus);
        jsonObject.addProperty("elytraFallResistanceBonus", elytraFallResistanceBonus);
        jsonObject.addProperty("speedAttackMultiplier", speedAttackMultiplier);

        try (Writer writer = new FileWriter(configPath.toFile())) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(jsonObject, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Getters
    public float getArmorWeight(String armorType) {
        return armorWeights.getOrDefault(armorType, 0f);
    }

    public boolean isEnableCounterAttributes() {
        return enableCounterAttributes;
    }

    public boolean isEnableSpeedJump() {
        return enableSpeedJump;
    }

    public float getElytraSpeedBonus() {
        return elytraSpeedBonus;
    }

    public float getElytraFallResistanceBonus() {
        return elytraFallResistanceBonus;
    }

    public float getSpeedAttackMultiplier() {
        return speedAttackMultiplier;
    }

    // Setters (if you want to allow programmatic changes)
    public void setArmorWeight(String armorType, float weight) {
        armorWeights.put(armorType, weight);
    }

    public void setEnableCounterAttributes(boolean enableCounterAttributes) {
        this.enableCounterAttributes = enableCounterAttributes;
    }

    public void setEnableSpeedJump(boolean enableSpeedJump) {
        this.enableSpeedJump = enableSpeedJump;
    }

    public void setElytraSpeedBonus(float elytraSpeedBonus) {
        this.elytraSpeedBonus = elytraSpeedBonus;
    }

    public void setElytraFallResistanceBonus(float elytraFallResistanceBonus) {
        this.elytraFallResistanceBonus = elytraFallResistanceBonus;
    }

    public void setSpeedAttackMultiplier(float speedAttackMultiplier) {
        this.speedAttackMultiplier = speedAttackMultiplier;
    }
}