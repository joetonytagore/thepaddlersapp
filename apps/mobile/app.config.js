import 'dotenv/config';

export default ({ config }) => {
  return {
    ...config,
    expo: {
      ...(config.expo || {}),
      extra: {
        ...(config.expo ? config.expo.extra : {}),
        stripePublishableKey: process.env.STRIPE_PUBLISHABLE_KEY || process.env.EXPO_PUBLIC_STRIPE_PUBLISHABLE_KEY || "",
      },
      plugins: [ 'stripe-react-native' ],
    }
  };
};

