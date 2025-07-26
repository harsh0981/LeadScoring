package org.example;

import java.util.*;
import java.util.logging.Logger;

public class LeadScoringApp {
    // --- MODEL CLASS ---
    static class Lead {
        String name;
        String source;
        String industry;
        int engagementLevel;

        public Lead(String name, String source, String industry, int engagementLevel) {
            this.name = name;
            this.source = source;
            this.industry = industry;
            this.engagementLevel = engagementLevel;
        }
    }

    // --- RULE INTERFACE ---
    interface Rule {
        int apply(Lead lead);
    }

    // --- RULE IMPLEMENTATIONS ---
    static class SourceRule implements Rule {
        private static final String UNKNOWN = "unknown";
        private static final String PURCHASED = "purchased";
        public int apply(Lead lead) {
            String source = lead.source.toLowerCase();
            if (source.equals("referral") || source.equals("organic") || source.equals("website")) return 40;
            if (source.equals("ads") || source.equals("email")) return 20;
            if (source.equals(UNKNOWN) || source.equals(PURCHASED)) return -10;
            return 0;
        }
    }

    static class IndustryRule implements Rule {
        private static final String UNKNOWN = "unknown";
        public int apply(Lead lead) {
            String industry = lead.industry.toLowerCase();
            if (industry.equals("tech")) return 40;
            if (industry.equals("finance")) return 35;
            if (industry.equals("education")) return 30;
            if (industry.equals("generic") || industry.equals(UNKNOWN)) return -15;
            return 20;
        }
    }

    static class EngagementRule implements Rule {
        public int apply(Lead lead) {
            int e = lead.engagementLevel;
            if (e == 100) return 60; // Perfect engagement bonus
            if (e >= 80) return 50;
            if (e >= 50) return 30;
            if (e >= 30) return 15;
            return -20; // Poor engagement penalty
        }
    }

    // --- NEW COMPLEX RULE ---
    static class CombinedRiskRule implements Rule {
        private static final String UNKNOWN = "unknown";
        private static final String PURCHASED = "purchased";
        public int apply(Lead lead) {
            if ((lead.source.equalsIgnoreCase(PURCHASED) || lead.source.equalsIgnoreCase(UNKNOWN))
                    && lead.engagementLevel < 30) {
                return -50; // High-risk lead penalty
            }
            return 0;
        }
    }

    // --- SCORING SERVICE ---
    static class LeadScoringService {
        private List<Rule> rules;

        public LeadScoringService() {
            this.rules = new ArrayList<>();
        }

        public void addRule(Rule rule) {
            rules.add(rule);
        }

        public int evaluate(Lead lead) {
            int totalScore = 0;
            for (Rule rule : rules) {
                totalScore += rule.apply(lead);
            }
            return totalScore;
        }
    }

    private static final Logger logger = Logger.getLogger(LeadScoringApp.class.getName());

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            LeadScoringService service = new LeadScoringService();

            // Add complex rules
            service.addRule(new SourceRule());
            service.addRule(new IndustryRule());
            service.addRule(new EngagementRule());
            service.addRule(new CombinedRiskRule());

            List<Lead> leads = new ArrayList<>();
            Map<Lead, Integer> leadScores = new HashMap<>();

            logger.info("Enter number of leads to process: ");
            int n;
            try {
                n = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException _) {
                logger.severe("Invalid number.");
                return;
            }

            for (int i = 1; i <= n; i++) {
                if (logger.isLoggable(java.util.logging.Level.INFO)) {
                    logger.info(String.format("--- Enter details for Lead %d ---", i));
                }
                logger.info("Name: ");
                String name = scanner.nextLine();

                logger.info("Source (Referral, Website, Organic, Ads, Email, Purchased, Unknown): ");
                String source = scanner.nextLine();

                logger.info("Industry (Tech, Finance, Education, Generic, Unknown): ");
                String industry = scanner.nextLine();

                logger.info("Engagement Level (0-100): ");
                int engagement;
                try {
                    engagement = Integer.parseInt(scanner.nextLine());
                    if (engagement < 0 || engagement > 100) {
                        logger.warning("Engagement must be between 0 and 100.");
                        return;
                    }
                } catch (NumberFormatException _) {
                    logger.warning("Invalid engagement level.");
                    return;
                }

                Lead lead = new Lead(name, source, industry, engagement);
                leads.add(lead);
                int score = service.evaluate(lead);
                leadScores.put(lead, score);
            }

            // Sort leaderboard
            logger.info("======= LEAD SCORE LEADERBOARD =======");
            leads.sort((a, b) -> leadScores.get(b) - leadScores.get(a));
            int rank = 1;
            for (Lead lead : leads) {
                if (logger.isLoggable(java.util.logging.Level.INFO)) {
                    logger.info(String.format("%d. %s | Score: %d", rank++, lead.name, leadScores.get(lead)));
                }
            }
        }
    }
}
