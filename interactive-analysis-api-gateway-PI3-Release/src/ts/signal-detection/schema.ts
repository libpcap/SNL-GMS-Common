/**
 * GraphQL schema definition for the signal detection API gateway
 */
import { resolve } from 'path';
import { readFileSync } from 'fs';

// GraphQL schema definitions
export const schema =
    readFileSync(resolve(process.cwd(), 'resources/graphql/signal-detection/schema.graphql')).toString();
