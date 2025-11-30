import 'dotenv/config';

export default ({ config }) => {
  const expo = {
    ...(config.expo || {}),
    ios: {
      ...((config.expo && config.expo.ios) || {}),
      bundleIdentifier: process.env.IOS_BUNDLE_IDENTIFIER || 'com.thepaddlers.mobile',
    },
    android: {
      ...((config.expo && config.expo.android) || {}),
      package: process.env.ANDROID_PACKAGE || 'com.thepaddlers.mobile',
    },
    extra: {
      ...((config.expo && config.expo.extra) || {}),
      stripePublishableKey: process.env.STRIPE_PUBLISHABLE_KEY || process.env.EXPO_PUBLIC_STRIPE_PUBLISHABLE_KEY || "",
    },
    plugins: [ ['@stripe/stripe-react-native', { merchantIdentifier: process.env.APPLE_PAY_MERCHANT_ID || '', enableGooglePay: false }] , ...((config.expo && config.expo.plugins) || [])]
  };

  return {
    ...config,
    expo,
  };
};
