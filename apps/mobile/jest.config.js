module.exports = {
  preset: 'react-native',
  roots: ['<rootDir>/src'],
  transform: {
    '^.+\\.(js|ts|tsx)$': 'babel-jest'
  },
  testEnvironment: 'jsdom',
  setupFilesAfterEnv: [
    require.resolve('@testing-library/jest-native/extend-expect')
  ],
  moduleFileExtensions: ['ts', 'tsx', 'js', 'jsx', 'json', 'node']
};

