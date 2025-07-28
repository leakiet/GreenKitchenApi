package com.greenkitchen.portal.utils;

import java.text.Normalizer;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

@Component
public class SlugUtils {

    private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");
    private static final Pattern EDGESDHASHES = Pattern.compile("(^-|-$)");

    public static String toSlug(String input) {
        if (input == null || input.trim().isEmpty()) {
            return "";
        }

        String nowhitespace = WHITESPACE.matcher(input).replaceAll("-");
        String normalized = Normalizer.normalize(nowhitespace, Normalizer.Form.NFD);
        String slug = NONLATIN.matcher(normalized).replaceAll("");
        slug = EDGESDHASHES.matcher(slug).replaceAll("");
        return slug.toLowerCase();
    }

    public static String generateUniqueSlug(String baseSlug, java.util.function.Function<String, Boolean> existsChecker) {
        String slug = baseSlug;
        int counter = 1;
        
        while (existsChecker.apply(slug)) {
            slug = baseSlug + "-" + counter;
            counter++;
        }
        
        return slug;
    }
}
