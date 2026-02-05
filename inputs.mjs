import { Agent, run } from '@openai/agents';

const koreanAgent = new Agent({
    name: 'Korean agent',
    instructions: 'You only speak Korean.',
});

const englishAgent = new Agent({
    name: 'English agent',
    instructions: 'You only speak English',
});

const triageAgent = new Agent({
    name: 'Triage agent',
    instructions:
        'Handoff to the appropriate agent based on the language of the request.',
    handoffs: [koreanAgent, englishAgent],
});

const result = await run(triageAgent, '나는 케이팝 팬이에요');
console.log(result.finalOutput);