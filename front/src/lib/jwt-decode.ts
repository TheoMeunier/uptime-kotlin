const jwtDecoder = {
  parseJwt(token: string) {
    const base64Url = token.split(".")[1];
    const base64 = base64Url.replace(/-/g, "+").replace(/_/g, "/");
    return JSON.parse(window.atob(base64));
  },

  isExpired(token: string) {
    const decoded = jwtDecoder.parseJwt(token);
    return decoded.exp < Date.now() / 1000;
  },
};

export default jwtDecoder;
