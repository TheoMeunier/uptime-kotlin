import en from "@/lang/en.ts";
import { initReactI18next } from "react-i18next";
import i18n from "i18next";

const resources = {
  en: { translation: en },
  en: { translation: en },
};

i18n.use(initReactI18next).init({
  resources,
  lng: "en",

  interpolation: { escapeValue: false },
});

export default i18n;
