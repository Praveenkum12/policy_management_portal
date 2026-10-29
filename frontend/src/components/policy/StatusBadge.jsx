import { STATUS_TONES } from '../../constants/policy';
import { Badge } from '../common';

export default function StatusBadge({ status }) {
  return <Badge tone={STATUS_TONES[status] ?? 'neutral'}>{status}</Badge>;
}
