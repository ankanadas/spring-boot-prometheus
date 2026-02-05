# English to Bengali Translator

A simple and effective English to Bengali translator using OpenAI's API.

## Features

- **Accurate Translation**: Uses GPT-4 for high-quality English to Bengali translations
- **Multiple Interfaces**: Command line, interactive terminal, and web interface
- **Batch Translation**: Translate multiple texts at once
- **Error Handling**: Robust error handling for API failures

## Setup

1. **Install Dependencies**
   ```bash
   npm install
   ```

2. **Set OpenAI API Key**
   ```bash
   export OPENAI_API_KEY="your-api-key-here"
   ```
   
   Or create a `.env` file:
   ```
   OPENAI_API_KEY=your-api-key-here
   ```

## Usage

### 1. Interactive Terminal Mode
```bash
npm run interactive
# or
npm start
```

### 2. Direct Translation (Code)
```bash
node translator.mjs
```

### 3. Web Interface
Open `web-translator.html` in your browser for a visual interface.

### 4. Programmatic Usage
```javascript
import { EnglishToBengaliTranslator } from './translator.mjs';

const translator = new EnglishToBengaliTranslator();

// Single translation
const bengali = await translator.translate("Hello, how are you?");
console.log(bengali); // Output: হ্যালো, আপনি কেমন আছেন?

// Batch translation
const results = await translator.translateBatch([
    "Good morning",
    "Thank you",
    "Have a nice day"
]);
```

## Examples

| English | Bengali |
|---------|---------|
| Hello | হ্যালো |
| Good morning | সুপ্রভাত |
| Thank you | ধন্যবাদ |
| How are you? | আপনি কেমন আছেন? |
| I love you | আমি তোমাকে ভালোবাসি |
| Goodbye | বিদায় |

## API Reference

### EnglishToBengaliTranslator

#### Methods

- `translate(englishText)` - Translates a single English text to Bengali
- `translateBatch(englishTexts)` - Translates an array of English texts

#### Parameters

- `englishText` (string) - The English text to translate
- `englishTexts` (array) - Array of English texts for batch translation

#### Returns

- Single translation: Bengali string
- Batch translation: Array of objects with `original`, `translation`, and optional `error` fields

## Requirements

- Node.js 14+
- OpenAI API key
- Internet connection

## Notes

- The translator uses GPT-4 for high accuracy
- Maintains cultural context and proper Bengali grammar
- Handles both formal and informal translations appropriately
- Supports Bengali script (বাংলা) output

## Error Handling

The translator includes comprehensive error handling for:
- Network connectivity issues
- API rate limits
- Invalid input text
- Authentication errors