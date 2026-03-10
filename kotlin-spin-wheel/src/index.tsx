import { requireNativeComponent, ViewProps } from 'react-native';

interface SpinWheelProps extends ViewProps {
  configUrl: string;
}

export const SpinWheelView = requireNativeComponent<SpinWheelProps>(
  'SpinWheelView'
);
