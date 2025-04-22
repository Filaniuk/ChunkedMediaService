# Encrypted Media Fetching with Privacy Preservation

This service effectively handles Range headers from browser requests, fetching the corresponding encrypted media chunk from Amazon S3 or returning the full file when the Range is null. Each request is processed using chat session keys stored in the HttpSession, which are never persisted in any database — ensuring user privacy.

The service also supports dynamic media streaming, allowing the frontend to progressively display download progress. Decryption is handled client-side using the user's private key, maintaining end-to-end confidentiality.
