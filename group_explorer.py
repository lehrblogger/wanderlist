import sys
import getpass
import atom
import gdata.contacts
import gdata.contacts.service

MAX_CONTACTS = 2000

class GroupExplorer(object):
    def __init__(self, username, password):
        self.username = username
        self.gd_client = gdata.contacts.service.ContactsService()
        self.gd_client.email = self.username
        self.gd_client.password = password
        self.gd_client.source = 'wanderlist-0.1'
        self.gd_client.ProgrammaticLogin()

        self.group_dict = {}
        self.fill_group_dict()

        self.groups_to_include = {}
        self.groups_to_exclude = {}

    def fill_group_dict(self):
        feed = self.gd_client.GetGroupsFeed()
        for entry in feed.entry:
            self.group_dict[entry.content.text] = entry.id.text

    def print_all_groups(self):
        print '  %s has the following groups:' % self.username
        for group in self.group_dict.keys():
            print '  %s' % (group)

    def populate_group_lists(self, includes, excludes):
        def check_group_list(name, group_list):
            print '%s:' % name
            temp_dict = {}
            for group in group_list:
                if group in self.group_dict:
                    print '  %s - "%s"' % (group, self.group_dict[group])
                    temp_dict[group] = self.group_dict[group]
                else:
                    print '  %s has no group named "%s"' % (self.username, group)
            return temp_dict

        self.groups_to_include = check_group_list('includes', includes)
        self.groups_to_exclude = check_group_list('excludes', excludes)

    def print_contact_set(self):
        def get_contacts_from_groups(group_dict, contacts_to_ignore=None):
            temp_dict = {}
            for group in group_dict.keys():
                query = gdata.contacts.service.ContactsQuery(group=self.group_dict[group])
                feed = self.gd_client.GetContactsFeed('%s&max-results=%d' % (query.ToUri(), MAX_CONTACTS))
                for entry in feed.entry:
                    if contacts_to_ignore and entry.id.text in contacts_to_ignore:
                        print '  Excluding %s!' % entry.title.text
                    else:
                        temp_dict[entry.id.text] = entry
            return temp_dict

        print '\n'
        excludeds = get_contacts_from_groups(self.groups_to_exclude)
        includeds = get_contacts_from_groups(self.groups_to_include, contacts_to_ignore=excludeds)
        print '\n'
        self.print_contact_entries(includeds.values())
        print '\n'

    def print_contact_entries(self, contact_entries):
        for i, entry in enumerate(contact_entries):
            print '%s %s (%s)' % (i+1, entry.title.text, entry.id.text)
        # print '\n%s %s' % (i+1, entry.title.text)
        # print '    %s' % (entry.id.text)
        # for email in entry.email:
        #     print '    %s' % (email.address)

def main():
    sys.argv.pop(0)
    group_list = sys.argv

    explorer = GroupExplorer('lehrburger', getpass.getpass())
    print '\n'

    if len(group_list) > 0:
        include_list = []
        exclude_list = []
        for group in group_list:
            if group.startswith('+'):
                include_list.append(group.lstrip('+'))
            elif group.startswith('-'):
                exclude_list.append(group.lstrip('-'))
        explorer.populate_group_lists(includes = include_list, excludes = exclude_list)
        explorer.print_contact_set()
    else:
        "List groups as arguments you wish to include or exclude, preceded by '+' and '-' respectively."
        explorer.print_all_groups()

if __name__ == '__main__':
    main()

