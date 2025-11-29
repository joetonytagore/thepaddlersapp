// Wrap console.warn early to filter a noisy, known deprecation message from expo-constants
const _originalConsoleWarn = console.warn;
console.warn = function (...args) {
  try {
    const first = args[0];
    if (typeof first === 'string' && first.includes("Constants.platform.ios.model has been deprecated")) {
      // swallow this specific deprecation warning (it's non-blocking and comes from expo-constants)
      return;
    }
  } catch (e) {
    // If anything goes wrong, fall back to original warn
  }
  return _originalConsoleWarn.apply(console, args);
};

import { registerRootComponent } from 'expo';

import App from './App';

// registerRootComponent calls AppRegistry.registerComponent('main', () => App);
// It also ensures that whether you load the app in Expo Go or in a native build,
// the environment is set up appropriately
registerRootComponent(App);
