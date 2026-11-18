ALTER TABLE status_pages
    ADD COLUMN custom_domain VARCHAR(253);

-- Several pages may have no domain (NULL); a given domain points to one page only.
CREATE UNIQUE INDEX ux_status_pages_custom_domain ON status_pages (custom_domain);
