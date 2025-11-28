import 'dotenv/config';

export default ({ config }) => {
  return {
    ...config,
    ios: {
      ...(config.ios || {}),
      bundleIdentifier: process.env.IOS_BUNDLE_IDENTIFIER || 'com.anonymous.thepaddlers-mobile',
    },
    // Top-level extra (Expo will merge this into the manifest's `extra` object)
    extra: {
      ...(config.extra || {}),
      stripePublishableKey: process.env.STRIPE_PUBLISHABLE_KEY || process.env.EXPO_PUBLIC_STRIPE_PUBLISHABLE_KEY || "",
    },
    // Ensure the stripe native plugin is applied during prebuild
    plugins: [
      ['@stripe/stripe-react-native', { merchantIdentifier: process.env.APPLE_PAY_MERCHANT_ID || '', enableGooglePay: false }]
    ]
  };
};
