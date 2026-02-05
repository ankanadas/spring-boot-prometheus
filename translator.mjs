import OpenAI from "openai";
const client = new OpenAI();

class EnglishToBengaliTranslator {
    constructor() {
        this.model = "gpt-4";
    }

    async translate(englishText) {
        try {
            const response = await client.chat.completions.create({
                model: this.model,
                messages: [
                    {
                        role: "system",
                        content: `You are a professional English to Bengali translator. 
                        Translate the given English text to Bengali accurately while maintaining:
                        - Natural Bengali grammar and sentence structure
                        - Cultural context and appropriate tone
                        - Proper Bengali script (বাংলা)
                        
                        Only return the Bengali translation, nothing else.`
                    },
                    {
                        role: "user",
                        content: englishText
                    }
                ],
                temperature: 0.3,
                max_tokens: 1000
            });

            return response.choices[0].message.content.trim();
        } catch (error) {
            console.error("Translation error:", error);
            throw new Error(`Failed to translate: ${error.message}`);
        }
    }

    async translateBatch(englishTexts) {
        const translations = [];
        for (const text of englishTexts) {
            try {
                const translation = await this.translate(text);
                translations.push({ original: text, translation });
            } catch (error) {
                translations.push({ original: text, error: error.message });
            }
        }
        return translations;
    }
}

// Example usage
const translator = new EnglishToBengaliTranslator();

// Single translation
const singleText = "Hello, how are you today?";
console.log("English:", singleText);

try {
    const bengaliTranslation = await translator.translate(singleText);
    console.log("Bengali:", bengaliTranslation);
} catch (error) {
    console.error("Error:", error.message);
}

// Batch translation example
const englishTexts = [
    "Good morning!",
    "Thank you for your help.",
    "I love learning new languages.",
    "The weather is beautiful today."
];

console.log("\n--- Batch Translation ---");
try {
    const batchResults = await translator.translateBatch(englishTexts);
    batchResults.forEach((result, index) => {
        console.log(`${index + 1}. English: ${result.original}`);
        if (result.translation) {
            console.log(`   Bengali: ${result.translation}`);
        } else {
            console.log(`   Error: ${result.error}`);
        }
        console.log();
    });
} catch (error) {
    console.error("Batch translation error:", error.message);
}

export { EnglishToBengaliTranslator };