UPDATE reservations SET deleted_at = NULL WHERE deleted_at IS NULL OR deleted_at IS NOT TRUE;

