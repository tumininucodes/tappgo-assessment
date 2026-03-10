const { withAndroidManifest, withPlugins } = require('@expo/config-plugins');

const withWidgetManifest = (config) => {
    return withAndroidManifest(config, (config) => {
        const mainApplication = config.modResults.manifest.application[0];

        // Add Widget Provider Receiver
        if (!mainApplication.receiver) mainApplication.receiver = [];
        if (!mainApplication.receiver.find((r) => r.$['android:name'] === 'com.kotlinspinwheel.SpinWheelWidgetProvider')) {
            mainApplication.receiver.push({
                $: {
                    'android:name': 'com.kotlinspinwheel.SpinWheelWidgetProvider',
                    'android:exported': 'true',
                    'android:label': 'Spin Wheel Widget',
                },
                'intent-filter': [
                    {
                        action: [
                            {
                                $: { 'android:name': 'android.appwidget.action.APPWIDGET_UPDATE' },
                            },
                        ],
                    },
                ],
                'meta-data': [
                    {
                        $: {
                            'android:name': 'android.appwidget.provider',
                            'android:resource': '@xml/spin_wheel_widget_info',
                        },
                    },
                ],
            });
        }

        // Add Animation Service
        if (!mainApplication.service) mainApplication.service = [];
        if (!mainApplication.service.find((s) => s.$['android:name'] === 'com.kotlinspinwheel.SpinWheelAnimationService')) {
            mainApplication.service.push({
                $: {
                    'android:name': 'com.kotlinspinwheel.SpinWheelAnimationService',
                    'android:foregroundServiceType': 'specialUse',
                    'android:exported': 'false',
                },
            });
        }

        return config;
    });
};

module.exports = (config) => withPlugins(config, [withWidgetManifest]);
