package com.kahanchale.splitexpense.config;

import com.kahanchale.splitexpense.entity.SplitExpense;
import com.kahanchale.splitexpense.strategy.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class SplitExpenseConfig {

    @Bean
    public Map<SplitExpense.SplitType, SplitStrategy> splitStrategies(
            EqualSplitStrategy equalSplitStrategy,
            ExactSplitStrategy exactSplitStrategy,
            PercentageSplitStrategy percentageSplitStrategy) {
        return Map.of(
                SplitExpense.SplitType.EQUAL, equalSplitStrategy,
                SplitExpense.SplitType.EXACT, exactSplitStrategy,
                SplitExpense.SplitType.PERCENTAGE, percentageSplitStrategy
        );
    }
}