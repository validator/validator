function urlencode(raw) {
    raw = escape(raw);
    raw = raw.replace(/\+/g, '%2B');
    raw = raw.replace(/\*/g, '%2A');
    raw = raw.replace(/\//g, '%2F');
    raw = raw.replace(/@/g, '%40');
    return raw;
}