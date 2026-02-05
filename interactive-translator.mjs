import { EnglishToBengaliTranslator } from './translator.mjs';
import readline from 'readline';

const translator = new EnglishToBengaliTranslator();

const rl = readline.createInterface({
    input: process.stdin,
    output: process.stdout
});

console.log("🌍 English to Bengali Translator");
console.log("Type 'quit' or 'exit' to stop\n");

function askForTranslation() {
    rl.question("Enter English text to translate: ", async (input) => {
        const trimmedInput = input.trim();
        
        if (trimmedInput.toLowerCase() === 'quit' || trimmedInput.toLowerCase() === 'exit') {
            console.log("Goodbye! 🙏");
            rl.close();
            return;
        }
        
        if (!trimmedInput) {
            console.log("Please enter some text to translate.\n");
            askForTranslation();
            return;
        }
        
        try {
            console.log("Translating...");
            const bengaliText = await translator.translate(trimmedInput);
            console.log(`\n📝 English: ${trimmedInput}`);
            console.log(`🇧🇩 Bengali: ${bengaliText}\n`);
        } catch (error) {
            console.error(`❌ Translation failed: ${error.message}\n`);
        }
        
        askForTranslation();
    });
}

askForTranslation();