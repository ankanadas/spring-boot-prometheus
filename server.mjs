import { createServer } from 'http';
import { readFileSync } from 'fs';
import { extname } from 'path';

const port = 3000;

const mimeTypes = {
    '.html': 'text/html',
    '.css': 'text/css',
    '.js': 'application/javascript',
    '.mjs': 'application/javascript'
};

const server = createServer((req, res) => {
    let filePath = req.url === '/' ? '/web-translator.html' : req.url;
    
    try {
        const content = readFileSync('.' + filePath);
        const ext = extname(filePath);
        const contentType = mimeTypes[ext] || 'text/plain';
        
        res.writeHead(200, { 'Content-Type': contentType });
        res.end(content);
    } catch (error) {
        res.writeHead(404);
        res.end('File not found');
    }
});

server.listen(port, () => {
    console.log(`🌐 Server running at http://localhost:${port}`);
    console.log(`📱 Open http://localhost:${port} in your browser`);
});