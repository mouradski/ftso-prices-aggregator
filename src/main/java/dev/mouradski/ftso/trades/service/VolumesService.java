package dev.mouradski.ftso.trades.service;

import jakarta.enterprise.context.ApplicationScoped;
import org.apache.commons.collections4.keyvalue.MultiKey;
import org.apache.commons.collections4.map.MultiKeyMap;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;

@ApplicationScoped
public class VolumesService {

    private final MultiKeyMap<String, Double> volumesPerExchangeBaseQuoteWeights = new MultiKeyMap<>();
    private final MultiKeyMap<String, Double> volumesPerExchangeBaseWeights = new MultiKeyMap<>();
    private final MultiKeyMap<String, Double> volumesPerExchanges = new MultiKeyMap<>();
    private final MultiKeyMap<String, Double> totalVolumesPerPair = new MultiKeyMap<>();

    public Pair<Double, Double> updateVolumes(String exchange, String base, String quote, Double amount) {

        var vwKey = new MultiKey<String>(exchange, base, quote);
        var tvppKey = new MultiKey<String>(base, quote);
        var vpebwKey = new MultiKey<String>(exchange, base);

        volumesPerExchangeBaseQuoteWeights.putIfAbsent(vwKey, 0d);
        volumesPerExchanges.putIfAbsent(vwKey, 0d);
        totalVolumesPerPair.putIfAbsent(tvppKey, 0d);
        volumesPerExchangeBaseWeights.putIfAbsent(vpebwKey, 0d);

        totalVolumesPerPair.put(tvppKey, totalVolumesPerPair.get(tvppKey) + amount);
        volumesPerExchanges.put(vwKey, volumesPerExchanges.get(vwKey) + amount);

        double weightExchangeBaseQuote = volumesPerExchanges.get(vwKey) / totalVolumesPerPair.get(tvppKey);

		double totalBaseVolume = volumesPerExchangeBaseQuoteWeights.entrySet().stream()
                .filter(v -> v.getKey().getKey(1).equals(base))
                .mapToDouble(Map.Entry::getValue)
                .sum();

        double volumeByExchangeBase = volumesPerExchangeBaseQuoteWeights.entrySet().stream()
                .filter(v -> v.getKey().getKey(0).equals(exchange) && v.getKey().getKey(1).equals(base))
                .mapToDouble(Map.Entry::getValue)
                .sum();

        volumesPerExchangeBaseQuoteWeights.put(vwKey, weightExchangeBaseQuote);


        volumesPerExchangeBaseWeights.put(vpebwKey, totalBaseVolume);

        double weightExchangeBase = volumeByExchangeBase / totalBaseVolume;

        return Pair.of(volumesPerExchangeBaseQuoteWeights.get(vwKey), weightExchangeBase);
    }

    //TODO
    public void resetVolumes() {
    }

}
